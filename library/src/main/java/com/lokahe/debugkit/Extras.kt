package com.lokahe.debugkit

import android.util.Log

internal fun String.fixInTab(numOfTab: Int): String =
    this + "\t".repeat(1.coerceAtLeast(numOfTab - length / 4))

internal fun String.fixInLen(len: Int): String =
    this + " ".repeat(1.coerceAtLeast(len - length + 1))

internal fun minSize(vararg lists: List<String>): Int =
    lists.minOf { list -> list.size }

internal fun List<String>.maxStrLen(): Int = maxOf { it.length }

internal fun calcMaxLen(vararg lists: List<String>): List<Int> =
    lists.map { list -> list.maxStrLen() }

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