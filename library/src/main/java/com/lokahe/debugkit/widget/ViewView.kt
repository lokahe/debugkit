package com.lokahe.debugkit.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Point
import android.graphics.PointF
import android.graphics.Rect
import android.os.Build
import android.text.TextPaint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
import android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
import android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
import android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
import android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
import android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
import androidx.core.graphics.toColorInt
import com.lokahe.debugkit.ViewHierarchyUtils
import com.lokahe.debugkit.add
import com.lokahe.debugkit.getRect
import com.lokahe.debugkit.getResName
import com.lokahe.debugkit.hierarchyId
import com.lokahe.debugkit.not0
import com.lokahe.debugkit.offset
import com.lokahe.debugkit.seeable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.sqrt

class ViewView(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    val infoTextSize = 32f
    val paint by lazy { Paint().apply { style = Paint.Style.STROKE; strokeWidth = 2f } }
    val selectedPaint by lazy { Paint().apply { style = Paint.Style.FILL } }
    val textPaint1 by lazy {
        TextPaint().apply {
            textSize = infoTextSize
            color = Color.WHITE
            isAntiAlias = true
        }
    }
    val textPaint2 by lazy {
        TextPaint().apply {
            textSize = infoTextSize
            color = Color.BLACK
            isAntiAlias = true
        }
    }
    val prevXY by lazy { Point() }
    var prevTime = 0L
    var speed: PointF = PointF()
    var moveLen = 0.0
    val movedMap = mutableMapOf<View, Pair<PointF, PointF>>()
    val layoutParams by lazy {
        WindowManager.LayoutParams(
            MATCH_PARENT, // Width
            MATCH_PARENT, // Height
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            // Flags to ensure it draws over status/nav bars and doesn't steal focus
            FLAG_NOT_FOCUSABLE or
                    FLAG_NOT_TOUCH_MODAL or
                    FLAG_LAYOUT_NO_LIMITS or
                    FLAG_LAYOUT_IN_SCREEN, // Allows drawing in the screen space (including nav/status bars)
            PixelFormat.TRANSLUCENT // Use an appropriate pixel format (TRANSLUCENT for overlays, OPAQUE for solid)
        ).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode = LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            }
        }
    }

    //    val displayCutout by lazy { Rect(0, 0, 0, 0) }
    var decorView: View? = null
        set(value) {
            field = value
            movedMap.clear()
            invalidate() // This runs every time you do: viewView.decorView = someView
        }

    var selectedView: View? = null

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        decorView?.let { decorView ->
            subviews(root = decorView) { v ->
                paint.color = String.format("#%06X", (0xFFFFFF and v.hashCode())).toColorInt()
                selectedPaint.color =
                    "#60${String.format("%06X", (0xFFFFFF and v.hashCode()))}".toColorInt()
                movedRect(v).let { rect ->
                    canvas.drawRect(rect, if (v == selectedView) selectedPaint else paint)
                    if (selectedView?.hierarchyId?.startsWith(v.hierarchyId) ?: false) {
                        "[${v.hierarchyId}]${v.getResName()}(${v.javaClass.simpleName})".let {
                            val textWidth =
                                Rect().apply { textPaint1.getTextBounds(it, 0, it.length, this) }
                                    .width() + 2
                            canvas.drawText(
                                it,
                                rect.left.coerceIn(0, width - textWidth).toFloat(),
                                rect.top.toFloat(),
                                textPaint1
                            )
                            canvas.drawText(
                                it,
                                rect.left.coerceIn(0, width - textWidth).toFloat() + 2,
                                rect.top.toFloat() + 2,
                                textPaint2
                            )
                        }
                    }
                }
            }
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                moveLen = 0.0
                prevTime = System.currentTimeMillis()
                prevXY.set(event.rawX.toInt(), event.rawY.toInt())
                selectedView = null
                invalidate()
                decorView?.let { decorView ->
                    mutableListOf<View>().apply { subviews(root = decorView) { add(it) } }
                        .forEach { v ->
                            if (movedRect(v).contains(event.rawX.toInt(), event.rawY.toInt()) &&
                                v.hierarchyId.length > (selectedView?.hierarchyId?.length ?: 0)
                            ) {
                                selectedView = v
                                movedMap[v]?.second?.set(PointF(0f, 0f))
                            }
                        }
                    ViewHierarchyUtils.logParents(selectedView)
                    invalidate()
                }
            }

            MotionEvent.ACTION_MOVE -> {
                Triple(
                    event.rawX.toInt() - prevXY.x,
                    event.rawY.toInt() - prevXY.y,
                    System.currentTimeMillis() - prevTime
                ).let { (dx, dy, dt) ->
                    moveLen += sqrt((dx * dx + dy * dy).toDouble())
                    speed.x = dx.toFloat() / dt
                    speed.y = dy.toFloat() / dt
                    selectedView?.let { v ->
                        movedMap[v]?.first?.add(dx.toFloat(), dy.toFloat()) ?: movedMap.put(
                            v, Pair(PointF(dx.toFloat(), dy.toFloat()), PointF(0f, 0f))
                        )
                    }
                }
                prevTime = System.currentTimeMillis()
                prevXY.set(event.rawX.toInt(), event.rawY.toInt())
                invalidate()
            }

            MotionEvent.ACTION_UP -> {
                selectedView?.let { slv ->
                    if (moveLen > 5) {
                        if (Rect(0, 0, width, height).contains(movedRect(slv))) {
                            movedMap[slv]?.second?.set(PointF(speed.x, speed.y))
                        }
                    } else {
                        movedMap.filter { (v, pair) -> v.hierarchyId.startsWith(slv.hierarchyId) }
                            .forEach { (v, pair) ->
                                movedMap[v]?.first?.set(0f, 0f)
                            }
                    }
                }
            }
        }
        return true
    }

    private fun movedRect(view: View): Rect = view.getRect().apply {
        movedMap.filter { (v, pair) -> view.hierarchyId.startsWith(v.hierarchyId) }
            .forEach { (v, pair) -> offset(pair.first) }
    }

    private fun subviews(root: View, onView: (View) -> Unit) {
        if (root is ViewGroup) {
            for (i in 0 until root.childCount) {
                subviews(root.getChildAt(i), onView)
            }
        }
        if (root.seeable) onView(root)
    }

    fun resetMoved() {
        movedMap.clear()
    }

    val friction = 0.1f
    val delay = 10L
    suspend fun slowdownJob() {
        withContext(Dispatchers.Main) {
            while (true) {
                movedMap.forEach { v, pair ->
                    if (pair.second.x != 0f || pair.second.y != 0f) {
                        pair.first.add(pair.second.x * delay, pair.second.y * delay)
                        movedRect(v).let { r ->
                            Pair(
                                not0(r.left.coerceAtMost(0), (r.right - width).coerceAtLeast(0)),
                                not0(r.top.coerceAtMost(0), (r.bottom - height).coerceAtLeast(0))
                            ).let { (dx, dy) ->
                                if (dx != 0) {
                                    pair.first.x -= (2 * dx)
                                    pair.second.x *= -1
                                }
                                if (dy != 0) {
                                    pair.first.y -= (2 * dy)
                                    pair.second.y *= -1
                                }
                            }
                        }
                        invalidate()
                        sqrt(pair.second.x * pair.second.x + pair.second.y * pair.second.y).let { spd ->
                            Pair(
                                friction * abs(pair.second.x) / spd,
                                friction * abs(pair.second.y) / spd
                            ).let { (dx, dy) ->
                                if (pair.second.x > 0)
                                    pair.second.x = (pair.second.x - dx).coerceAtLeast(0f)
                                else
                                    pair.second.x = (pair.second.x + dx).coerceAtMost(0f)
                                if (pair.second.y > 0)
                                    pair.second.y = (pair.second.y - dy).coerceAtLeast(0f)
                                else
                                    pair.second.y = (pair.second.y + dy).coerceAtMost(0f)
                            }
                        }
                    }
                }
                delay(delay)
            }
        }
    }
}