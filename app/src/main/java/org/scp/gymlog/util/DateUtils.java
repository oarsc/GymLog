package org.scp.gymlog.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtils {
    public static int[] yearsAndDaysDiff(Calendar d1, Calendar d2) {
        Calendar d1p = getFirstTimeOfDay(d1);
        Calendar d2p = getFirstTimeOfDay(d2);
        int currentYear = d1p.get(Calendar.YEAR);

        int diffDays = (int) (Math.abs(d1p.getTimeInMillis() - d2p.getTimeInMillis()) / 86400000L);
        int diffYears = 0;

        int yearDays;
        while ((yearDays = isLeapYear(currentYear)? 366 : 365) < diffDays) {
            diffDays -= yearDays;
            diffYears++;
            currentYear++;
        }

        return new int[] {diffYears, diffDays};
    }

    public static boolean isLeapYear(int year) {
        return (year % 4 == 0 && year % 100 != 0) || year % 400 == 0;
    }

    public static Calendar getFirstTimeOfDay(Calendar date) {
        Calendar mDate = (Calendar) date.clone();
        mDate.set(Calendar.HOUR_OF_DAY, 0);
        mDate.set(Calendar.MINUTE, 0);
        mDate.set(Calendar.SECOND, 0);
        mDate.set(Calendar.MILLISECOND, 0);
        return mDate;
    }

    public static String getTime(Calendar cal) {
        Date date = cal.getTime();
        SimpleDateFormat format1 = new SimpleDateFormat("HH:MM", Locale.getDefault());
        return format1.format(date);
    }
}
