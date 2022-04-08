package org.scp.gymlog.model

import lombok.Getter
import org.scp.gymlog.util.WeightFormatter
import org.scp.gymlog.util.WeightUtils
import java.math.BigDecimal
import java.util.function.Function

@Getter
class Weight(val value: BigDecimal, val internationalSystem: Boolean) : Comparable<Weight> {

    companion object {
        val INVALID = Weight(BigDecimal(-1), true)
    }

    val valid: Boolean get() = value < BigDecimal.ZERO

    fun getValue(internationalSystem: Boolean): BigDecimal {
        return if (internationalSystem) toKg() else toLbs()
    }

    fun getValue(internationalSystem: Boolean, formatter: WeightFormatter): BigDecimal {
        return if (internationalSystem) toKg(formatter) else toLbs(formatter)
    }

    fun toKg(): BigDecimal {
        return if (internationalSystem) value else WeightUtils.toKilograms(value)
    }

    fun toLbs(): BigDecimal {
        return if (internationalSystem) WeightUtils.toPounds(value) else value
    }

    private fun toKg(formatter: WeightFormatter): BigDecimal {
        return if (internationalSystem) value else WeightUtils.toKilograms(value, formatter)
    }

    private fun toLbs(formatter: WeightFormatter): BigDecimal {
        return if (internationalSystem) WeightUtils.toPounds(value, formatter) else value
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