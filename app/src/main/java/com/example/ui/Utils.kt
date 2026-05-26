package com.example.ui

import java.text.DecimalFormat

object FormatUtils {
    
    // Format full numeric scales to compact readable strings (e.g. 1.25B, 400.2M)
    fun formatCompact(value: Double): String {
        return when {
            value >= 1e12 -> DecimalFormat("$#,##0.00'T'").format(value / 1e12)
            value >= 1e9 -> DecimalFormat("$#,##0.0'B'").format(value / 1e9)
            value >= 1e6 -> DecimalFormat("$#,##0.0'M'").format(value / 1e6)
            value >= 1e3 -> DecimalFormat("$#,##0").format(value)
            else -> DecimalFormat("$#,##0.00").format(value)
        }
    }

    fun formatLargeNumberRaw(value: Double): String {
        return when {
            value >= 1e12 -> DecimalFormat("#,##0.0'T'").format(value / 1e12)
            value >= 1e9 -> DecimalFormat("#,##0.0'B'").format(value / 1e9)
            value >= 1e6 -> DecimalFormat("#,##0.0'M'").format(value / 1e6)
            else -> DecimalFormat("#,##0").format(value)
        }
    }

    fun formatPrice(value: Double): String {
        return when {
            value >= 1000 -> DecimalFormat("$#,##0").format(value)
            value >= 1.0 -> DecimalFormat("$#,##0.00").format(value)
            else -> DecimalFormat("$#,##0.0000").format(value)
        }
    }

    fun formatPercentage(value: Double): String {
        val df = DecimalFormat("+0.00%;-0.00%")
        return df.format(value / 100.0)
    }
}
