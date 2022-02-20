package org.scp.gymlog.util;

import static org.scp.gymlog.util.Constants.LBS_RATIO;
import static org.scp.gymlog.util.Constants.TWO;
import static org.scp.gymlog.util.WeightFormatter.DEFAULT_WEIGHT_FORMATTER;
import static org.scp.gymlog.util.WeightFormatter.EXACT_FORMATTER;
import static org.scp.gymlog.util.WeightFormatter.TWO_DECS_FORMATTER;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import org.scp.gymlog.R;
import org.scp.gymlog.model.Bar;
import org.scp.gymlog.model.Weight;
import org.scp.gymlog.model.WeightSpecification;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class WeightUtils {
    private final static int DEFAULT_SCALE = 2;

    private static boolean exactConversion = false;
    private static BigDecimal conversionStep = new BigDecimal("1");

    public static void setConvertParameters(boolean exactConversion, @NonNull String conversionStep) {
        WeightUtils.exactConversion = exactConversion;
        WeightUtils.conversionStep = new BigDecimal(conversionStep);
    }

    @StringRes
    public static int unit(boolean internationalSystem) {
        return internationalSystem? R.string.text_kg : R.string.text_lb;
    }

    public static BigDecimal toKilograms(Weight weight) {
        return weight.toKg();
    }

    public static BigDecimal toKilograms(BigDecimal pounds) {
        return toKilograms(pounds, DEFAULT_WEIGHT_FORMATTER);
    }

    public static BigDecimal toKilograms(BigDecimal pounds, WeightFormatter formatter) {
        if (formatter.isExactScale())
            return pounds.divide(LBS_RATIO, LBS_RATIO.scale(), RoundingMode.HALF_UP);

        if (formatter.isForceScale())
            return pounds.divide(LBS_RATIO, formatter.getScale(), RoundingMode.HALF_UP);

        if (exactConversion)
            return pounds.divide(LBS_RATIO, DEFAULT_SCALE, RoundingMode.HALF_UP);

        return pounds.divide(LBS_RATIO, LBS_RATIO.scale(), RoundingMode.HALF_UP)
                .divide(conversionStep, conversionStep.scale(), RoundingMode.HALF_UP)
                .multiply(conversionStep);
    }

    public static BigDecimal toPounds(Weight weight) {
        return weight.toLbs();
    }

    public static BigDecimal toPounds(BigDecimal kilograms) {
        return toPounds(kilograms, DEFAULT_WEIGHT_FORMATTER);
    }

    public static BigDecimal toPounds(BigDecimal kilograms, WeightFormatter formatter) {
        if (formatter.isExactScale())
            return kilograms.multiply(LBS_RATIO);

        if (formatter.isForceScale())
            return kilograms.multiply(LBS_RATIO).setScale(formatter.getScale(), RoundingMode.HALF_UP);

        if (exactConversion)
            return kilograms.multiply(LBS_RATIO).setScale(DEFAULT_SCALE, RoundingMode.HALF_UP);

        return kilograms.multiply(LBS_RATIO)
                .divide(conversionStep, conversionStep.scale(), RoundingMode.HALF_UP)
                .setScale(0, RoundingMode.HALF_UP)
                .multiply(conversionStep);
    }

    public static BigDecimal getTotalWeight(BigDecimal value, WeightSpecification weightSpec,
                                      Bar bar, boolean internationalSystem) {
        switch (weightSpec) {
            case NO_BAR_WEIGHT:
                return bar == null? value :
                        value.add(bar.getWeight().getValue(internationalSystem));
            case ONE_SIDE_WEIGHT:
                return bar == null? value.add(value) :
                        value.add(value).add(bar.getWeight().getValue(internationalSystem));
            default:
                return value;
        }
    }

    public static BigDecimal getWeightFromTotal(Weight weight, WeightSpecification weightSpec,
                                                Bar bar, boolean internationalSystem) {
        return getRawWeight(weight, weightSpec, bar).getValue(internationalSystem);
    }

    public static BigDecimal getWeightFromTotalDefaultScaled(Weight weight, WeightSpecification weightSpec,
                                                Bar bar, boolean internationalSystem) {
        return getRawWeight(weight, weightSpec, bar).getValue(internationalSystem, TWO_DECS_FORMATTER);
    }

    private static Weight getRawWeight(Weight weight, WeightSpecification weightSpec, Bar bar) {
        switch (weightSpec) {
            case ONE_SIDE_WEIGHT:
                if (bar != null) {
                    BigDecimal barWeight = bar.getWeight().getValue(weight.isInternationalSystem(), EXACT_FORMATTER);
                    return weight.op(v -> v.subtract(barWeight).divide(TWO, 2, RoundingMode.HALF_UP));
                }
                return weight.op(v -> v.divide(TWO, 2, RoundingMode.HALF_UP));
            case NO_BAR_WEIGHT:
                if (bar != null) {
                    BigDecimal barWeight = bar.getWeight().getValue(weight.isInternationalSystem(), EXACT_FORMATTER);
                    return weight.op(v -> v.subtract(barWeight));
                }
            default:
                return weight;
        }
    }
}
