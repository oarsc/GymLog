package org.scp.gymlog.util;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

public class Constants {
    public static final BigDecimal TWO = new BigDecimal("2");
    public static final BigDecimal FIVE = new BigDecimal("5");
    public static final BigDecimal ONE_THOUSAND = new BigDecimal("1000");
    public static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    public static final BigDecimal LBS_RATIO = new BigDecimal("2.2046226218488");
    public static final Calendar DATE_ZERO;
    public static final Calendar DATE_FUTURE;

    static {
        DATE_ZERO = Calendar.getInstance();
        DATE_ZERO.setTimeInMillis(0L);

        DATE_FUTURE = Calendar.getInstance();
        DATE_FUTURE.setTimeInMillis(32503590000000L);
    }

    public enum IntentReference {
        NONE,
        REGISTRY,
        EXERCISE_LIST,
        CREATE_EXERCISE,
        CREATE_EXERCISE_FROM_MUSCLE,
        EDIT_EXERCISE,
        IMAGE_SELECTOR,
        TRAINING,
        TOP_RECORDS,

        SAVE_FILE,
        LOAD_FILE
    }
}
