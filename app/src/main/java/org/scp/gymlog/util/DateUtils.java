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

        int diffDays = (int) (Math.abs(diff(d1p, d2p)) / 86400000L);
        int diffYears = 0;

        int yearDays;
        while ((yearDays = isLeapYear(currentYear)? 366 : 365) < diffDays) {
            diffDays -= yearDays;
            diffYears++;
            currentYear++;
        }

        return new int[] {diffYears, diffDays};
    }

    public static int secondsDiff(Calendar earliest, Calendar latest) {
        return (int) (diff(earliest, latest) / 1000L);
    }

    public static long diff(Calendar earliest, Calendar latest) {
        return latest.getTimeInMillis() - earliest.getTimeInMillis();
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
        SimpleDateFormat format1 = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return format1.format(date);
    }

    public static String getDateTime(Calendar cal) {
        Date date = cal.getTime();
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        return format1.format(date);
    }

    public static String getDate(Calendar cal) {
        Date date = cal.getTime();
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return format1.format(date);
    }

    public static String calculateTimeLetter(Calendar date, Calendar today) {
        if (date == null || date.compareTo(Constants.DATE_ZERO) == 0) {
            return "";
        }
        int[] todayDiff = yearsAndDaysDiff(date, today);
        if (todayDiff[0] == 0) {
            if (todayDiff[1] == 0) return "T";
            else                   return todayDiff[1]+"D";
        }
        return todayDiff[0]+ "Y" + todayDiff[1]+"D";
    }
}
