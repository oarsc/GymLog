package org.scp.gymlog.util

import android.util.DisplayMetrics
import android.util.TypedValue
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.Format
import java.text.ParseException
import java.util.*

object FormatUtils {

    private val FORMAT: Format
    init {
        val decimalFormat = DecimalFormat.getInstance(Locale.getDefault()) as DecimalFormat
        decimalFormat.isParseBigDecimal = true
        decimalFormat.isGroupingUsed = false
        FORMAT = decimalFormat
    }

    fun toBigDecimal(string: String): BigDecimal {
        return if (string.isEmpty())
            BigDecimal.ZERO
        else {
            try {
                FORMAT.parseObject(string) as BigDecimal
            } catch (e: NumberFormatException) {
                e.printStackTrace()
                BigDecimal.ZERO
            } catch (e: ParseException) {
                e.printStackTrace()
                BigDecimal.ZERO
            }
        }
    }

    fun toInt(string: String): Int {
        return try {
            string.toInt()
        } catch (e: NumberFormatException) {
            e.printStackTrace()
            0
        }
    }

    fun toString(bigDecimal: BigDecimal): String {
        return FORMAT.format(bigDecimal)
    }

    fun toDp(displayMetrics: DisplayMetrics, value: Int): Int {
        return toDpFloat(displayMetrics, value).toInt()
    }

    fun toDpFloat(displayMetrics: DisplayMetrics, value: Int): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value.toFloat(),
            displayMetrics
        )
    }

    @SafeVarargs
    fun <T> isAnyOf(needle: T, vararg haystack: T): Boolean {
        return listOf(*haystack).contains(needle)
    }
}