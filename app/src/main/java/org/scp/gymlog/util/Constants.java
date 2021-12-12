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

    static {
        DATE_ZERO = Calendar.getInstance();
        DATE_ZERO.setTimeInMillis(0L);
    }
}
