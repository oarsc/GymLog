package org.scp.gymlog.util

import android.util.DisplayMetrics
import android.util.TypedValue
import android.widget.TextView
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

    fun String.safeBigDecimal(): BigDecimal {
        return if (this.isBlank())
            BigDecimal.ZERO
        else {
            try {
                FORMAT.parseObject(this) as BigDecimal
            } catch (e: NumberFormatException) {
                e.printStackTrace()
                BigDecimal.ZERO
            } catch (e: ParseException) {
                e.printStackTrace()
                BigDecimal.ZERO
            }
        }
    }

    fun String.safeInt(): Int {
        return try {
            this.toInt()
        } catch (e: NumberFormatException) {
            0
        }
    }

    fun BigDecimal.toLocaleString(): String {
        return FORMAT.format(this);
    }

    var TextView.bigDecimal: BigDecimal
        get() = this.text.toString().safeBigDecimal()
        set(value) { text = value.toLocaleString() }

    var TextView.integer: Int
        get() = this.text.toString().safeInt()
        set(value) { text = value.toString() }

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

    fun <T> T.isAnyOf(vararg haystack: T): Boolean {
        return listOf(*haystack).contains(this)
    }
}