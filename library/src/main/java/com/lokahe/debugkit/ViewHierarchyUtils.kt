package com.lokahe.debugkit

import android.graphics.Rect
import android.view.View
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.core.view.isVisible


val INDEX = (('0'..'9') + ('a'..'z') + ('A'..'Z')).toList()

val View.hierarchyId: String
    get() = if (parent is View) {
        (parent as View).hierarchyId + INDEX[(parent as ViewGroup).indexOfChild(this)]
    } else ""

internal fun View.childCount(): Int = if (this is ViewGroup) childCount else 0

internal fun View.getResName(): String =
    if (id != 0 && id != -1)
        try {
            resources.getResourceEntryName(id)
        } catch (e: Exception) {
            "($id)"
        }
    else "($id)"

private fun View.getRectStr(): String =
    intArrayOf(0, 0).let {
        getLocationOnScreen(it)
        Rect(it[0], it[1], width, height)
    }.let { rect ->
        "x:${rect.left},y:${rect.top},w:${rect.right},h:${rect.bottom}"
    }

private fun View.visibilityStr(): String =
    when (visibility) {
        VISIBLE -> "visible"
        INVISIBLE -> "invisible"
        GONE -> "gone"
        else -> ""
    }

private fun View.enableStr(): String = if (isEnabled) "enable" else "disable"
private fun View.clickableStr(): String = if (isClickable) "clickable" else "unclickable"
private fun View.layoutParamsHeightStr(): String = this@layoutParamsHeightStr.layoutParams?.let {
    when (it.height) {
        MATCH_PARENT -> "MATCH_PARENT"
        WRAP_CONTENT -> "WRAP_CONTENT"
        else -> it.height.toString()
    }
} ?: ""

private fun View.layoutParamsWidthStr(): String = this@layoutParamsWidthStr.layoutParams?.let {
    when (it.width) {
        MATCH_PARENT -> "MATCH_PARENT"
        WRAP_CONTENT -> "WRAP_CONTENT"
        else -> it.width.toString()
    }
} ?: ""

class ViewHierarchyUtils {
    companion object {
        private val TAG = ViewHierarchyUtils::class.java.simpleName
        private val COLS_LEN = 10

        @JvmStatic
        @JvmOverloads
        fun readViewInfo(
            view: View?,
            prefix: String = "",
            cols: MutableList<MutableList<String>> = MutableList(COLS_LEN) { mutableListOf() }
        ): String =
            view?.let { v ->
                cols[0].add("${prefix.spaceIfNoEmpty()}${view.hierarchyId}")
                cols[1].add("[${v.childCount()}]")
                cols[2].add(v.getRectStr())
                cols[3].add(v.visibilityStr())
                cols[4].add(v.enableStr())
                cols[5].add(v.clickableStr())
                cols[6].add("${v.layoutParamsWidthStr()}/${v.layoutParamsHeightStr()}")
                cols[7].add(v.getResName())
                cols[8].add(v.javaClass.getName() + "@" + v.hashCode())
                cols[9].add(v.contentStr())
                return cols.joinToString(" ")
            } ?: "$prefix[null]"

        @JvmStatic
        @JvmOverloads
        fun logViewInfo(
            view: View?,
            tag: String? = null,
            prefix: String = ""
        ) {
            (MutableList(COLS_LEN) { mutableListOf<String>() }).let {
                readViewInfo(view, prefix, it)
                logD(tag ?: TAG, { prefix }, *it.toTypedArray())
            }
        }

        @JvmStatic
        @JvmOverloads
        fun logParents(
            view: View?,
            tag: String? = null,
            prefix: String = ""
        ) {
            MutableList(COLS_LEN) { mutableListOf<String>() }.let {
                logParentsRecursive(view, it, tag, prefix)
                logD(tag ?: TAG, { "" }, *it.toTypedArray())
            }
        }

        private fun logParentsRecursive(
            view: View?,
            cols: MutableList<MutableList<String>> = MutableList(COLS_LEN) { mutableListOf() },
            tag: String? = null,
            prefix: String = ""
        ) {
            view?.let { v ->
                v.parent?.let { parent ->
                    if (parent is View) {
                        logParentsRecursive(parent, cols, tag, prefix)
                    }
                }
                readViewInfo(v, prefix, cols)
            }
        }

        @JvmStatic
        @JvmOverloads
        fun logAllSubViews(
            view: View?,
            hasSize: Boolean = false,
            visible: Boolean = false,
            tag: String? = null,
            prefix: String = ""
        ) {
            MutableList(COLS_LEN) { mutableListOf<String>() }.let { cols ->
                readSubViewRecursive(
                    view,
                    cols,
                    hasSize,
                    visible,
                    tag,
                    prefix
                )
                logD(tag ?: TAG, { "" }, *cols.toTypedArray())
            }
        }

        internal fun readSubViewRecursive(
            view: View?,
            cols: MutableList<MutableList<String>> = MutableList(COLS_LEN) { mutableListOf() },
            hasSize: Boolean = false,
            visible: Boolean = false,
            tag: String? = null,
            prefix: String = ""
        ) {
            view?.let { v ->
                if ((!hasSize || !v.hasSize) && (!visible || v.isVisible)) {
                    readViewInfo(v, prefix, cols)
                }
                if (v is ViewGroup) {
                    for (i in 0..v.childCount()) {
                        val subView = v.getChildAt(i)
                        readSubViewRecursive(
                            subView,
                            cols,
                            hasSize,
                            visible,
                            tag,
                            prefix
                        )
                    }
                }
            } ?: "$prefix[null]"
        }
    }
}
