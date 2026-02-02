package com.lokahe.debugkit

import android.graphics.Insets
import android.graphics.Point
import android.graphics.Rect
import android.util.Log
import android.view.View
import android.widget.TextView
import java.io.Serializable

internal fun String.fixInTab(numOfTab: Int): String =
    this + "\t".repeat(1.coerceAtLeast(numOfTab - length / 4))

internal fun String.fixInLen(len: Int): String =
    this + " ".repeat(1.coerceAtLeast(len - length + 1))

fun String.spaceIfNoEmpty(): String {
    return if (isNotEmpty() && !endsWith(" ")) {
        "$this "
    } else this
}

internal fun minSize(vararg lists: List<String>): Int =
    lists.minOf { list -> list.size }

internal fun List<String>.maxStrLen(): Int = maxOf { it.length }

internal fun calcMaxLen(vararg lists: List<String>): List<Int> =
    lists.map { list -> if (list.isEmpty()) 0 else list.maxStrLen() }

internal fun logD(tag: String, prefix: (Int) -> String = { "" }, vararg cols: List<String>) {
    val maxLen = calcMaxLen(*cols)
    for (i in 0 until minSize(*cols)) {
        Log.d(tag, prefix(i) + StringBuilder().apply {
            for (j in cols.indices) {
                append(cols[j][i].fixInLen(maxLen[j]))
            }
        }.toString())
    }
}

data class Quadruple<out A, out B, out C, out D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
) : Serializable {
    /**
     * Returns string representation of the [Pair] including its [first] and [second] values.
     */
    public override fun toString(): String = "($first, $second, $third, $fourth)"
}

data class Fivetuple<out A, out B, out C, out D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: D
) : Serializable {
    /**
     * Returns string representation of the [Pair] including its [first] and [second] values.
     */
    public override fun toString(): String = "($first, $second, $third, $fourth, $fifth)"
}

internal fun View.getRect(): Rect =
    intArrayOf(0, 0).let {
        getLocationOnScreen(it)
        Rect(it[0], it[1], it[0] + width, it[1] + height)
    }

internal fun View.contentStr(maxLen: Int = 30): String =
    when (this) {
        is TextView
            -> text.toString().apply { if (length > maxLen) (substring(0, maxLen) + "...") }

        else
            -> ""
    }

fun Rect.contain(point: Point): Boolean =
    contains(point.x, point.y)

val View.hasSize: Boolean
    get() = width > 0 && height > 0

val View.seeable: Boolean
    get() = visibility == View.VISIBLE && hasSize

fun Insets.update(left: Int, top: Int, right: Int, bottom: Int) {
    this.left = left
    this.top = top
    this.right = right
    this.bottom = bottom
}