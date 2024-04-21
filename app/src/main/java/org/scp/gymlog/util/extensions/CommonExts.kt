package org.scp.gymlog.util.extensions

import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.sqrt

object CommonExts {
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
}