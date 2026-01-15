package com.lokahe.debugkit

import android.graphics.Rect
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.core.view.isVisible


val INDEX = (('0'..'9') + ('a'..'z') + ('A'..'Z')).toList()

private fun View.childCount(): Int = if (this is ViewGroup) childCount else 0
private fun View.getRect(): Rect =
    intArrayOf(0, 0).let {
        getLocationOnScreen(it)
        Rect(it[0], it[1], width, height)
    }

private fun View.getRectStr(): String =
    getRect().let { rect ->
        "x:${rect.left},y:${rect.top},w:${rect.right},h:${rect.bottom}"
    }

private fun View.getResName(): String =
    if (id != 0 && id != -1) resources.getResourceEntryName(id) else ""

private fun View.visibilityStr(): String =
    when (visibility) {
        VISIBLE -> "visible"
        INVISIBLE -> "invisible"
        GONE -> "gone"
        else -> ""
    }

private fun View.enableStr(): String = if (isEnabled) "enable" else "disable"
private fun View.clickableStr(): String = if (isClickable) "clickable" else "unclickable"
private fun View.layoutParamsHeightStr(): String = layoutParams?.let {
    when (it.height) {
        MATCH_PARENT -> "MATCH_PARENT"
        WRAP_CONTENT -> "WRAP_CONTENT"
        else -> it.height.toString()
    }
} ?: ""

private fun View.layoutParamsWidthStr(): String = layoutParams?.let {
    when (it.width) {
        MATCH_PARENT -> "MATCH_PARENT"
        WRAP_CONTENT -> "WRAP_CONTENT"
        else -> it.width.toString()
    }
} ?: ""

private fun String.fixInTab(numOfTab: Int): String =
    this + "\t".repeat(1.coerceAtLeast(numOfTab - length / 4))

class ViewHierarchyUtils {
    companion object {
        private val TAG = ViewHierarchyUtils::class.java.simpleName
        private val TABS = arrayOf(5, 8, 3, 2, 3, 9, 15)

        @JvmStatic
        @JvmOverloads
        fun readViewInfo(
            view: View?,
            prefix: String = "",
            divider: String = "",
            tabs: Array<Int> = TABS
        ): String =
            view?.let { v ->
                "$prefix[${v.childCount()}]".fixInTab(tabs[0]) +
                        "${divider}${v.getRectStr()}".fixInTab(tabs[1]) +
                        "${divider}${v.visibilityStr()}".fixInTab(tabs[2]) +
                        "${divider}${v.enableStr()}".fixInTab(tabs[3]) +
                        "${divider}${v.clickableStr()}".fixInTab(tabs[4]) +
                        "${divider}lp.w/h:${v.layoutParamsWidthStr()}/${v.layoutParamsHeightStr()}".fixInTab(
                            tabs[5]
                        ) +
                        "${divider}resId:${v.getResName()}(${v.id})".fixInTab(tabs[6]) +
                        "${divider}${v.javaClass.getName()}@${v.hashCode()}"
            } ?: "$prefix[null]"

        @JvmStatic
        @JvmOverloads
        fun logViewInfo(
            view: View?,
            tag: String = TAG,
            prefix: String = "",
            divider: String = "",
            tabs: Array<Int> = TABS
        ) {
            Log.d(tag, readViewInfo(view, prefix, divider, tabs))
        }

        @JvmStatic
        @JvmOverloads
        fun logParents(
            view: View?,
            tag: String = TAG,
            prefix: String = "",
            divider: String = "",
            tabs: Array<Int> = TABS
        ) {
            view?.let { v ->
                v.parent?.let { parent ->
                    if (parent is View) {
                        logParents(parent, tag, "$prefix↖", divider, tabs)
                    }
                }
                logViewInfo(v, tag, prefix, divider, tabs)
            }
        }

        @JvmStatic
        @JvmOverloads
        fun logAllSubViews(
            view: View?,
            hasSize: Boolean = false,
            visible: Boolean = false,
            tag: String = TAG,
            prefix: String = "",
            divider: String = "",
            tabs: Array<Int> = TABS
        ) {
            view?.let { v ->
                if ((!hasSize || !v.getRect().isEmpty) && (!visible || v.isVisible)) {
                    logViewInfo(v, tag, prefix, divider, tabs)
                }
                if (v is ViewGroup) {
                    for (i in 0..v.childCount()) {
                        val subView = v.getChildAt(i)
                        logAllSubViews(
                            subView,
                            hasSize,
                            visible,
                            tag,
                            prefix + INDEX[i % INDEX.size],
                            divider,
                            tabs
                        )
                    }
                }
            } ?: "$prefix[null]"
        }
    }
}