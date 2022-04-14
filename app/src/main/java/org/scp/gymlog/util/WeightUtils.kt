package org.scp.gymlog.util

import androidx.annotation.StringRes
import org.scp.gymlog.R
import org.scp.gymlog.model.Bar
import org.scp.gymlog.model.Weight
import org.scp.gymlog.model.WeightSpecification
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

    fun getTotalWeight(
        value: BigDecimal,
        weightSpec: WeightSpecification?,
        bar: Bar?,
        internationalSystem: Boolean
    ): BigDecimal {
        return when (weightSpec) {
            WeightSpecification.NO_BAR_WEIGHT ->
                if (bar == null)
                    value
                else
                    value.add(bar.weight.getValue(internationalSystem))

            WeightSpecification.ONE_SIDE_WEIGHT ->
                if (bar == null)
                    value.add(value)
                else
                    value.add(value).add(bar.weight.getValue(internationalSystem))

            else -> value
        }
    }

    fun getWeightFromTotal(
        weight: Weight,
        weightSpec: WeightSpecification,
        bar: Bar?,
        internationalSystem: Boolean
    ): BigDecimal {
        return getRawWeight(weight, weightSpec, bar).getValue(internationalSystem)
    }

    fun getWeightFromTotalDefaultScaled(
        weight: Weight,
        weightSpec: WeightSpecification,
        bar: Bar?,
        internationalSystem: Boolean
    ): BigDecimal {
        return getRawWeight(weight, weightSpec, bar)
            .getValue(internationalSystem, WeightFormatter.TWO_DECS_FORMATTER)
    }

    private fun getRawWeight(weight: Weight, weightSpec: WeightSpecification, bar: Bar?): Weight {
        return when (weightSpec) {
            WeightSpecification.ONE_SIDE_WEIGHT -> {
                if (bar != null) {
                    val barWeight = bar.weight.getValue(
                        weight.internationalSystem,
                        WeightFormatter.EXACT_FORMATTER
                    )
                    weight.op { v -> v.subtract(barWeight)
                        .divide(Constants.TWO, 2, RoundingMode.HALF_UP)
                    }
                } else {
                    weight.op { v -> v.divide(Constants.TWO,2, RoundingMode.HALF_UP) }
                }
            }
            WeightSpecification.NO_BAR_WEIGHT -> {
                if (bar != null) {
                    val barWeight = bar.weight.getValue(
                        weight.internationalSystem,
                        WeightFormatter.EXACT_FORMATTER
                    )
                    weight.op { v -> v.subtract(barWeight) }
                } else {
                    weight
                }
            }
            else -> weight
        }
    }
}