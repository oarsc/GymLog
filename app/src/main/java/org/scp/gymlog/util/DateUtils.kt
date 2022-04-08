package org.scp.gymlog.util

import org.scp.gymlog.util.Constants.DATE_ZERO
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

object DateUtils {

    fun yearsAndDaysDiff(d1: Calendar, d2: Calendar): IntArray {
        val d1p = getFirstTimeOfDay(d1)
        val d2p = getFirstTimeOfDay(d2)
        var currentYear = d1p[Calendar.YEAR]

        var diffDays = (abs(diff(d1p, d2p)) / 86400000L).toInt()
        var diffYears = 0

        @Suppress("ControlFlowWithEmptyBody")
        while (getYearDays(currentYear).let { yearDays ->
                if (diffDays > yearDays) {
                    diffDays -= yearDays
                    diffYears++
                    currentYear++
                    true
                } else false
        });

        return intArrayOf(diffYears, diffDays)
    }

    fun secondsDiff(earliest: Calendar, latest: Calendar): Int {
        return (diff(earliest, latest) / 1000L).toInt()
    }

    fun diff(earliest: Calendar, latest: Calendar): Long {
        return latest.timeInMillis - earliest.timeInMillis
    }

    private fun getYearDays(year: Int): Int {
        return if (isLeapYear(year)) 366 else 365;
    }

    private fun isLeapYear(year: Int): Boolean {
        return year % 4 == 0 && year % 100 != 0 || year % 400 == 0
    }

    fun getFirstTimeOfDay(date: Calendar): Calendar {
        return (date.clone() as Calendar).also {
            it[Calendar.HOUR_OF_DAY] = 0
            it[Calendar.MINUTE] = 0
            it[Calendar.SECOND] = 0
            it[Calendar.MILLISECOND] = 0
        }
    }

    fun getTime(cal: Calendar): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(cal.time)
    }

    fun getDateTime(cal: Calendar): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(cal.time)
    }

    fun getDate(cal: Calendar): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
    }

    fun calculateTimeLetter(date: Calendar?, today: Calendar): String {
        if (date == null || date.compareTo(DATE_ZERO) == 0) {
            return ""
        }

        val todayDiff = yearsAndDaysDiff(date, today)
        return if (todayDiff[0] == 0) {
                if (todayDiff[1] == 0) "T"
                else todayDiff[1].toString() + "D"
            } else todayDiff[0].toString() + "Y" + todayDiff[1] + "D"
    }
}