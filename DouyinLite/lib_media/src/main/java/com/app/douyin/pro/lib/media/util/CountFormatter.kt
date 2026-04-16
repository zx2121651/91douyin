package com.app.douyin.pro.lib.media.util

import java.util.Locale

object CountFormatter {
    fun format(count: Long): String {
        return when {
            count >= 100_000_000 -> String.format(Locale.getDefault(), "%.1f亿", count / 100_000_000.0)
            count >= 10_000 -> String.format(Locale.getDefault(), "%.1fw", count / 10_000.0)
            else -> count.toString()
        }
    }
}
