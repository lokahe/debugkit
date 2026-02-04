package com.lokahe.debugkit.widget

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Point
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
import com.lokahe.debugkit.Fivetuple
import com.lokahe.debugkit.R
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
    }

    private var switch = false
    private val windowManager by lazy { context.getSystemService(Context.WINDOW_SERVICE) as WindowManager }
    private val prevXY by lazy { Point() }
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

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        when (event?.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                alpha = 1f
                prevXY.x = event.rawX.toInt()
                prevXY.y = event.rawY.toInt()
                moveLen = 0.0
            }

            MotionEvent.ACTION_MOVE -> {
                Fivetuple(
                    windowManager.currentWindowMetrics.bounds,
                    layoutParams.x,
                    layoutParams.y,
                    event.rawX.toInt() - prevXY.x,
                    event.rawY.toInt() - prevXY.y
                ).let { (bounds, x, y, dx, dy) ->
                    // Only mark as moved if the touch actually shifted
                    moveLen += sqrt((dx * dx + dy * dy).toDouble())
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
                    switch = !switch
                    onClick(switch)
                    setImageResource(if (switch) R.drawable.logo_on else R.drawable.logo)
//                    context.setSystemGestureExclusionRects
                }
            }
        }
        return true
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

//    suspend fun sideJob() {
//        withContext(Dispatchers.Main) {
//            while (true) {
//                windowManager.currentWindowMetrics.bounds.let { bounds ->
//                    not0(
//                        (layoutParams.x - height / 2).coerceAtMost(0) * 0.01f,
//                        (layoutParams.x - (bounds.width() - height * 3 / 2)).coerceAtLeast(0) * 0.01f
//                    ).let { dx ->
//                        if (layoutParams.x == 0 || x.toInt() == bounds.width() - height / 2) {
//                            setImageResource(if (switch) R.drawable.logo_left_on else R.drawable.logo_left)
//                            if (layoutParams.x == bounds.width() - height / 2)
//                                scaleX = -1f
//                        } else if (dx != 0f && !isInTouchMode) {
//                            layoutParams.x = (layoutParams.x + dx).coerceIn(0f, (bounds.width() - height / 2).toFloat()).toInt()
//                        } else {
//                            setImageResource(if (switch) R.drawable.logo_on else R.drawable.logo)
//                        }
//                    }
//                }
//                delay(10)
//            }
//        }
//    }
}