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
        "x:${rect.left},y:${rect.top},w:${rect.right},h:${rect.bottom})"
    }

private fun View.getResName(): String =
    if (id != 0 || id != -1) resources.getResourceEntryName(id) else ""

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

class ViewHierarchyUtils {
    companion object {
        @JvmStatic
        @JvmOverloads
        fun readViewInfo(
            view: View?,
            prefix: String = "",
            separator: String = "\t",
            divider: String = "|"
        ): String =
            view?.let { v ->
                "$prefix[${v.childCount()}]" +
                        "$separator${v.getRectStr()}" +
                        "${divider}resId:${v.getResName()}(${v.id})" +
                        "${divider}${v.javaClass.getName()}@${v.hashCode()}" +
                        "${divider}${v.visibilityStr()}" +
                        "${divider}${v.enableStr()}" +
                        "${divider}${v.clickableStr()}" +
                        "${divider}lp.width/height:${v.layoutParamsWidthStr()}/${v.layoutParamsHeightStr()}"
            } ?: "$prefix[null]"

        @JvmStatic
        @JvmOverloads
        fun logViewInfo(
            view: View?,
            tag: String = this::class.java.simpleName,
            prefix: String = "",
            separator: String = "\t",
            divider: String = "|"
        ) {
            Log.d(tag, readViewInfo(view, prefix, separator, divider))
        }

        @JvmStatic
        @JvmOverloads
        fun logParents(
            view: View?,
            tag: String = this::class.java.simpleName,
            prefix: String = "",
            separator: String = "\t",
            divider: String = "|"
        ) {
            view?.let { v ->
                logViewInfo(v, tag, prefix, separator, divider)
                v.parent?.let { parent ->
                    if (parent is View) {
                        logParents(parent, tag, "$prefix↖", separator, divider)
                    }
                }
            }
        }

        @JvmStatic
        @JvmOverloads
        fun logAllSubViews(
            view: View?,
            hasSize: Boolean = false,
            visible: Boolean = false,
            tag: String = this::class.java.simpleName,
            prefix: String = "",
            separator: String = "\t",
            divider: String = "|"
        ) {
            view?.let { v ->
                if ((!hasSize || !v.getRect().isEmpty) && (!visible || v.isVisible)) {
                    logViewInfo(v, tag, prefix, separator, divider)
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
                            separator,
                            divider
                        )
                    }
                }
            } ?: "$prefix[null]"
        }
    }
}