package org.oar.gymlog.model

import org.oar.gymlog.util.WeightFormatter
import org.oar.gymlog.util.WeightUtils.toKilograms
import org.oar.gymlog.util.WeightUtils.toPounds
import java.math.BigDecimal
import java.util.function.Function

class Weight(val value: BigDecimal, val internationalSystem: Boolean) : Comparable<Weight> {

    companion object {
        val INVALID = Weight(BigDecimal(-1), true)
        val ZERO = Weight(BigDecimal(0), true)
    }

    val valid: Boolean get() = value < BigDecimal.ZERO

    fun getValue(internationalSystem: Boolean): BigDecimal {
        return if (internationalSystem) toKg() else toLbs()
    }

    fun getValue(internationalSystem: Boolean, formatter: WeightFormatter): BigDecimal {
        return if (internationalSystem) toKg(formatter) else toLbs(formatter)
    }

    fun toKg(): BigDecimal {
        return if (internationalSystem) value else value.toKilograms()
    }

    fun toLbs(): BigDecimal {
        return if (internationalSystem) value.toPounds() else value
    }

    private fun toKg(formatter: WeightFormatter): BigDecimal {
        return if (internationalSystem) value else value.toKilograms(formatter)
    }

    private fun toLbs(formatter: WeightFormatter): BigDecimal {
        return if (internationalSystem) value.toPounds(formatter) else value
    }

    fun add(weight: Weight): Weight {
        if (weight.internationalSystem != internationalSystem) {
            throw ArithmeticException("Can't operate with different units")
        }
        return Weight(value.add(weight.value), internationalSystem)
    }

    fun add(weight: Weight, internationalSystem: Boolean): Weight {
        return if (internationalSystem)
            Weight(toKg().add(weight.toKg()),true)
        else
            Weight(toLbs().add(weight.toLbs()), false)
    }

    fun subtract(weight: Weight): Weight {
        if (weight.internationalSystem != internationalSystem) {
            throw ArithmeticException("Can't operate with different units")
        }
        return Weight(value.subtract(weight.value), internationalSystem)
    }

    fun subtract(weight: Weight, internationalSystem: Boolean): Weight {
        return if (internationalSystem)
            Weight(toKg().subtract(weight.toKg()), true)
        else
            Weight(toLbs().subtract(weight.toLbs()),false)
    }

    fun op(operation: Function<BigDecimal, BigDecimal>): Weight {
        return Weight(
            operation.apply(value),
            internationalSystem
        )
    }

    override fun compareTo(other: Weight): Int {
        return value.compareTo(other.value)
    }
}