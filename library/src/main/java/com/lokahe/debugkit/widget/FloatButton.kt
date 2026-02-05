package com.lokahe.debugkit.widget

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Point
import android.graphics.PointF
import android.graphics.Rect
import android.os.Build
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.WindowInsets
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
import android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
import android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
import android.widget.ImageButton
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.doOnLayout
import com.lokahe.debugkit.R
import com.lokahe.debugkit.Sixuple
import com.lokahe.debugkit.multiply
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.math.sqrt

class FloatButton(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    val onClick: (Boolean) -> Unit
) : ImageButton(context, attrs, defStyleAttr) {
    init {
        setImageResource(R.drawable.logo)
        background = Color.TRANSPARENT.toDrawable()
        doOnLayout {
            layoutParams.width = width
            layoutParams.height = height
            size = height
            windowManager.updateViewLayout(this@FloatButton, layoutParams)
        }
        addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            updateState()
            systemGestureExclusionRects =
                listOf(Rect(0, 0, width, height))
            invalidate()
        }
    }

    private var size: Int = 0
    private var switch = false
    private var prevTime = 0L
    private var imgRes = 0
    private val speed = PointF()
    private val windowManager by lazy { context.getSystemService(Context.WINDOW_SERVICE) as WindowManager }
    private val prevXY by lazy { Point() }
    private val windowBounds
        get() = windowManager.currentWindowMetrics.bounds.apply {
            bottom -= displayCutout.bottom
        }
    private val displayCutout: Rect
        get() = windowManager.currentWindowMetrics.windowInsets.getInsets(
            WindowInsets.Type.displayCutout() or WindowInsets.Type.systemBars()
        ).let { cutoutInsets ->
            return Rect(
                cutoutInsets.left,
                cutoutInsets.top,
                cutoutInsets.right,
                cutoutInsets.bottom
            )
        }
    val layoutParams by lazy {
        WindowManager.LayoutParams(
            WRAP_CONTENT,
            WRAP_CONTENT,
            TYPE_APPLICATION_OVERLAY,
            FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.LEFT
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode = LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            }
        }
    }

    private var moveLen = 0.0
    private val movSpd = PointF(0f, 0f)
    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        when (event?.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                alpha = 1f
                prevTime = System.currentTimeMillis()
                speed.set(0f, 0f)
                movSpd.set(0f, 0f)
                prevXY.x = event.rawX.toInt()
                prevXY.y = event.rawY.toInt()
                moveLen = 0.0
                resize()
            }

            MotionEvent.ACTION_MOVE -> {
                Sixuple(
                    windowBounds,
                    layoutParams.x,
                    layoutParams.y,
                    event.rawX.toInt() - prevXY.x,
                    event.rawY.toInt() - prevXY.y,
                    System.currentTimeMillis() - prevTime
                ).let { (bounds, x, y, dx, dy, dt) ->
                    // Only mark as moved if the touch actually shifted
                    movSpd.set(dx.toFloat() / dt, dy.toFloat() / dt)
                    moveLen += sqrt((dx * dx + dy * dy).toDouble())
                    prevTime = System.currentTimeMillis()
                    layoutParams.x = (x + dx).coerceIn(
                        bounds.left,
                        bounds.right - width - displayCutout.left
                    )
                    layoutParams.y =
                        (y + dy).coerceIn(
                            bounds.top,
                            bounds.bottom - height - displayCutout.top
                        )
                }
                prevXY.x = event.rawX.toInt()
                prevXY.y = event.rawY.toInt()
                windowManager.updateViewLayout(this, layoutParams)
            }

            MotionEvent.ACTION_UP -> {
                if (moveLen < 5.0) {
                    // This satisfies accessibility requirements
                    updateState(!switch)
                    onClick(switch)
                    setImageResource(if (switch) R.drawable.logo_on else R.drawable.logo)
                } else {
                    speed.set(movSpd)
                }
            }
        }
        return true
    }

    private fun updateState(onOff: Boolean = switch) {
        switch = onOff
        val res = if (switch) {
            if (width == height / 2) R.drawable.logo_left_on
            else R.drawable.logo_on
        } else {
            if (width == height / 2) R.drawable.logo_left
            else R.drawable.logo
        }
        if (imgRes != res) {
            imgRes = res
            setImageResource(imgRes)
        }
    }

    private fun resize() {
        layoutParams.width = size
        layoutParams.height = size
        windowManager.updateViewLayout(this, layoutParams)
        updateState()
    }

    suspend fun transparentJob() {
        withContext(Dispatchers.Main) {
            while (true) {
                var t = 0f
                while (alpha > if (switch) 0.75f else 0.25f) {
                    if (alpha == 1f) {
                        alpha = 0.999f
                        t = 0f
                    } else {
                        alpha -= 0.001f * t
                        t += 0.001f
                    }
                    delay(10)
                }
                delay(100)
            }
        }
    }

    val friction = 0.01f
    val delay = 10L
    suspend fun sideJob() {
        withContext(Dispatchers.Main) {
            while (true) {
                delay(delay)
                if (speed.x == 0f && speed.y == 0f) continue
                if (speed.x != 0f) {
                    layoutParams.x += (speed.x * delay).toInt()
                    if (layoutParams.x < 0) {
                        layoutParams.x = 0
                        layoutParams.width = height / 2
                        speed.set(0f, 0f)
                        scaleX = 1f
                    } else if (layoutParams.x > windowBounds.right - width) {
                        layoutParams.x = windowBounds.right - height / 2
                        layoutParams.width = height / 2
                        speed.set(0f, 0f)
                        scaleX = -1f
                    }
                }
                if (speed.y != 0f) {
                    layoutParams.y += (speed.y * delay).toInt()
                    if (layoutParams.y < 0) {
                        layoutParams.y = 0
                        speed.y *= -1
                    } else if (layoutParams.y - height > windowBounds.bottom) {
                        layoutParams.y = windowBounds.bottom - height
                        speed.y *= -1
                    }
                }
                windowManager.updateViewLayout(this@FloatButton, layoutParams)
                speed.multiply(1 - friction, 1f / delay)
            }
        }
    }
}