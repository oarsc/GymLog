package org.scp.gymlog.util;

import lombok.Getter;

@Getter
public class WeightFormatter {
    private boolean forceScale;
    private int scale;
    private boolean exactScale;

    public static final WeightFormatter DEFAULT_WEIGHT_FORMATTER = new WeightFormatter(false);
    public static final WeightFormatter EXACT_FORMATTER = new WeightFormatter(true);
    public static final WeightFormatter TWO_DECS_FORMATTER = new WeightFormatter(2);

    private WeightFormatter(boolean exactScale) {
        this.exactScale = exactScale;
    }

    public WeightFormatter(int scale) {
        this.forceScale = true;
        this.scale = scale;
    }
}
