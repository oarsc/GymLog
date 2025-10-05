package org.oar.gymlog.util.extensions

import android.content.Context
import android.util.TypedValue
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.sqrt

object CommonExts {
    fun Pair<Float, Float>.distance(other: Pair<Float, Float>): Float {
        val dx = first - other.first
        val dy = second - other.second
        return sqrt(dx * dx + dy * dy)
    }

    fun BigDecimal.adjustScale(maxScale: Int = 2): BigDecimal {
        val realScale = realScale()
        return when {
            realScale > maxScale -> setScale(maxScale, RoundingMode.HALF_UP)
            realScale != scale() -> setScale(realScale, RoundingMode.HALF_UP)
            else -> this
        }
    }

    private fun BigDecimal.realScale(): Int {
        val valueStr = toPlainString()
        val decimalIndex = valueStr.indexOf('.')

        if (decimalIndex < 0) {
            return 0
        }

        var leftZeroes = 0
        for (i in valueStr.length - 1 downTo decimalIndex + 1) {
            if (valueStr[i] == '0') leftZeroes++
            else break
        }

        return scale() - leftZeroes
    }

    fun Context.getThemeColor(resId: Int): Int {
        val typedValue = TypedValue()
        theme.resolveAttribute(resId, typedValue, true)
        return typedValue.data
    }
}