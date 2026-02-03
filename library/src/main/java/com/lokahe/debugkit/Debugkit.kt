package com.lokahe.debugkit

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.appcompat.view.ContextThemeWrapper
import com.google.android.material.R
import com.lokahe.debugkit.widget.FloatButton
import com.lokahe.debugkit.widget.ViewView
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class Debugkit private constructor(
    val context: Context
) {
    private val TAG = Debugkit::class.java.simpleName
    private val windowManager by lazy { context.getSystemService(Context.WINDOW_SERVICE) as WindowManager }

    // Wrap the context with a Material/AppCompat theme
    private val fltBtn by lazy {
        FloatButton(
            ContextThemeWrapper(context, R.style.Theme_MaterialComponents_Light_NoActionBar)
        ) {
            if (it) {
                vView.resetMoved()
                windowManager.addView(vView, vView.layoutParams)
                showFloatingBtn()
            } else if (vView.windowToken != null) {
                try {
                    windowManager.removeView(vView)
                } catch (e: IllegalArgumentException) {
                    // Handle cases where the view was already removed by the system
                }
            }
        }
    }
    private val vView by lazy { ViewView(context) }
    private var jobs: Job? = null

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var instance: Debugkit? = null

        /**
         * Initialize or get the Debugkit instance.
         * Note: Use applicationContext to prevent memory leaks where possible,
         */
        @JvmStatic
        fun getInstance(context: Context): Debugkit {
            return instance ?: synchronized(this) {
                instance ?: Debugkit(context).also { instance = it }
            }
        }

        /**
         * Cleans up the instance to prevent memory leaks when the Activity is destroyed.
         */
        @JvmStatic
        fun destroy() {
            instance?.stop()
            instance = null
        }
    }

    private fun stop() {
        jobs?.cancel()
        try {
            if (fltBtn.parent != null) windowManager.removeView(fltBtn)
            if (vView.parent != null) windowManager.removeView(vView)
        } catch (e: Exception) {
            Log.e(TAG, "Cleanup error", e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun setDecorView(decorView: View? = null) {
        vView.decorView = decorView
        if (!Settings.canDrawOverlays(context)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            if (context !is android.app.Activity)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } else if (fltBtn.parent == null) {
            showFloatingBtn()
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun showFloatingBtn() {
        // Double check permission inside the private method to be safe
        if (!Settings.canDrawOverlays(context)) return
        if (fltBtn.parent != null) windowManager.removeView(fltBtn)
        if (jobs == null) jobs = MainScope().launch {
            launch { fltBtn.transparentJob() }
            launch { vView.slowdownJob() }
        }
        windowManager.addView(fltBtn, fltBtn.layoutParams)
    }
}
