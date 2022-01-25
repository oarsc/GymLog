package org.scp.gymlog.util;

import android.util.DisplayMetrics;
import android.util.TypedValue;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Locale;

public class FormatUtils {

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

    public static int toInt(String string) {
        try {
            return Integer.parseInt(string);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static String toString(BigDecimal bigDecimal) {
        return FORMAT.format(bigDecimal);
    }

    public static int toDp(DisplayMetrics displayMetrics, int value) {
        return (int) toDpFloat(displayMetrics, value);
    }

    public static float toDpFloat(DisplayMetrics displayMetrics, int value) {
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                displayMetrics
        );
    }

    @SafeVarargs
    public static <T> boolean isAnyOf(T needle, T... haystack) {
        return Arrays.asList(haystack).contains(needle);
    }
}
