package org.scp.gymlog.util;

import org.scp.gymlog.model.Weight;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.ParseException;
import java.util.Locale;

public class FormatUtils {
    public static final BigDecimal ONE_THOUSAND = new BigDecimal("1000");
    public static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    private static final BigDecimal LBS_RATIO = new BigDecimal("2.2046226218488");

    private static final Format FORMAT;
    static {
        DecimalFormat decimalFormat = (DecimalFormat) DecimalFormat.getInstance(Locale.getDefault());
        decimalFormat.setParseBigDecimal(true);
        decimalFormat.setGroupingUsed(false);
        FORMAT = decimalFormat;
    }

    public static BigDecimal toBigDecimal(String string) {
        if (string.isEmpty())
            return BigDecimal.ZERO;

        try {
            return (BigDecimal) FORMAT.parseObject(string);

        } catch (NumberFormatException | ParseException e) {
            e.printStackTrace();
            return BigDecimal.ZERO;
        }
    }

    public static String toString(BigDecimal bigDecimal) {
        return FORMAT.format(bigDecimal);
    }

    public static BigDecimal toKilograms(BigDecimal pounds) {
        return pounds.divide(LBS_RATIO, 2, RoundingMode.HALF_UP);
    }

    public static BigDecimal toKilograms(Weight weight) {
        return weight.toKg();
    }

    public static BigDecimal toPounds(BigDecimal kilograms) {
        return kilograms.multiply(LBS_RATIO).setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal toPounds(Weight weight) {
        return weight.toLbs();
    }
}
