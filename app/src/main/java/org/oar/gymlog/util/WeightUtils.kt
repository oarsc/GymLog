package org.oar.gymlog.util

import androidx.annotation.StringRes
import org.oar.gymlog.R
import org.oar.gymlog.model.Bar
import org.oar.gymlog.model.Weight
import org.oar.gymlog.model.WeightSpecification
import org.oar.gymlog.util.extensions.CommonExts.adjustScale
import java.math.BigDecimal
import java.math.RoundingMode

object WeightUtils {
    private const val DEFAULT_SCALE = 2
    private var exactConversion = false
    private var conversionStep = BigDecimal("1")

    fun setConvertParameters(exactConversion: Boolean, conversionStep: String) {
        WeightUtils.exactConversion = exactConversion
        WeightUtils.conversionStep = BigDecimal(conversionStep)
    }

    @StringRes
    fun unit(internationalSystem: Boolean): Int {
        return if (internationalSystem) R.string.text_kg else R.string.text_lb
    }


    fun BigDecimal.toKilograms(
        formatter: WeightFormatter = WeightFormatter.DEFAULT_WEIGHT_FORMATTER
    ) :BigDecimal {
        if (formatter.exactScale)
            return this.divide(
                Constants.LBS_RATIO,
                Constants.LBS_RATIO.scale(),
                RoundingMode.HALF_UP
            )

        if (formatter.forceScale)
            return this.divide(
                Constants.LBS_RATIO,
                formatter.scale,
                RoundingMode.HALF_UP
            )

        return if (exactConversion)
            this.divide(
                Constants.LBS_RATIO,
                DEFAULT_SCALE,
                RoundingMode.HALF_UP)
        else
            this.divide(
                Constants.LBS_RATIO,
                Constants.LBS_RATIO.scale(),
                RoundingMode.HALF_UP)
                .divide(conversionStep, conversionStep.scale(), RoundingMode.HALF_UP)
                .multiply(conversionStep)
    }

    fun BigDecimal.toPounds(
        formatter: WeightFormatter = WeightFormatter.DEFAULT_WEIGHT_FORMATTER
    ): BigDecimal {
        if (formatter.exactScale)
            return this.multiply(Constants.LBS_RATIO)

        if (formatter.forceScale)
            return this.multiply(Constants.LBS_RATIO)
                .setScale(formatter.scale, RoundingMode.HALF_UP)

        return if (exactConversion)
            this.multiply(Constants.LBS_RATIO)
                .setScale(DEFAULT_SCALE, RoundingMode.HALF_UP)
        else this.multiply(Constants.LBS_RATIO)
            .divide(conversionStep, conversionStep.scale(), RoundingMode.HALF_UP)
            .setScale(0, RoundingMode.HALF_UP)
            .multiply(conversionStep)
    }

    fun Weight.calculateTotal(
        weightSpec: WeightSpecification,
        bar: Bar?,
    ): Weight {
        if (weightSpec == WeightSpecification.TOTAL_WEIGHT) {
            return this
        }

        val barWeight = bar?.weight
            ?.getValue(this.internationalSystem)
            ?: BigDecimal.ZERO

        return this.op { it
            .divide(weightSpec.weightAffectation).adjustScale()
            .add(barWeight)
        }
    }

    /**
     * Converts totalWeight into the specific WeightSpecification and Bar
     */
    fun Weight.calculate(weightSpec: WeightSpecification, bar: Bar?): Weight {
        if (weightSpec == WeightSpecification.TOTAL_WEIGHT) {
            return this
        }

        val barWeight = bar?.weight
            ?.getValue(this.internationalSystem, WeightFormatter.EXACT_FORMATTER)
            ?: BigDecimal.ZERO

        return this.op { it
            .subtract(barWeight)
            .multiply(weightSpec.weightAffectation).adjustScale()
        }
    }

    fun Weight.defaultScaled(internationalSystem: Boolean): BigDecimal =
        this.getValue(internationalSystem, WeightFormatter.TWO_DECS_FORMATTER)

    fun convertWeight(
        weight: Weight,
        iWeightSpec: WeightSpecification,
        iBar: Bar?,
        fWeightSpec: WeightSpecification,
        fBar: Bar?): Weight {

        return weight
            .calculateTotal(iWeightSpec, iBar)
            .calculate(fWeightSpec, fBar)
    }
}