package com.lokahe.debugkit

import android.content.Context
import android.graphics.Insets
import android.graphics.Point
import android.graphics.PointF
import android.graphics.Rect
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import java.io.Serializable

internal fun String.fixInTab(numOfTab: Int): String =
    this + "\t".repeat(1.coerceAtLeast(numOfTab - length / 4))

internal fun String.fixInLen(len: Int): String =
    this + " ".repeat(1.coerceAtLeast(len - length + 1))

internal fun String.spaceIfNoEmpty(): String {
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

internal fun not0(a: Int, b: Int): Int = if (b != 0) b else a

internal fun Rect.contain(point: Point): Boolean =
    contains(point.x, point.y)

internal fun Rect.offset(pointF: PointF?): Rect {
    pointF?.let { offset(it.x.toInt(), it.y.toInt()) }
    return this
}

internal val View.hasSize: Boolean
    get() = width > 0 && height > 0

internal val View.seeable: Boolean
    get() = visibility == View.VISIBLE && hasSize

internal fun Point.set(point: Point) {
    set(point.x, point.y)
}

internal fun Point.add(x: Int, y: Int) {
    this.x += x
    this.y += y
}

internal fun PointF.add(x: Float, y: Float) {
    this.x += x
    this.y += y
}

internal fun Insets.update(left: Int, top: Int, right: Int, bottom: Int) {
    this.left = left
    this.top = top
    this.right = right
    this.bottom = bottom
}

@ColorInt
internal fun Context.getThemeColor(@AttrRes attribute: Int): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(attribute, typedValue, true)
    val colorRes = typedValue.run { if (resourceId != 0) resourceId else data }
    return ContextCompat.getColor(this, colorRes)
}