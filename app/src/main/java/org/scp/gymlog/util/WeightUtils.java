package org.scp.gymlog.util;

import static org.scp.gymlog.util.Constants.TWO;

import org.scp.gymlog.model.Bar;
import org.scp.gymlog.model.WeightSpecification;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class WeightUtils {
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

    public static BigDecimal getWeightFromTotal(BigDecimal total, WeightSpecification weightSpec,
                                                Bar bar, boolean internationalSystem) {
        switch (weightSpec) {
            case ONE_SIDE_WEIGHT:
                if (bar != null) {
                    return total.subtract(bar.getWeight().getValue(internationalSystem))
                            .divide(TWO, 2, RoundingMode.HALF_UP);
                }
                return total.divide(TWO, 2, RoundingMode.HALF_UP);
            case NO_BAR_WEIGHT:
                if (bar != null) {
                    return total.subtract(bar.getWeight().getValue(internationalSystem));
                }
            default:
                return total;
        }
    }
}
