package org.scp.gymlog.util

import org.scp.gymlog.util.Constants.DATE_ZERO
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import kotlin.math.roundToInt

object DateUtils {

    fun Calendar.diff(other: Calendar = Calendar.getInstance()): Long {
        return abs(this.timeInMillis - other.timeInMillis)
    }

    fun Calendar.diffSeconds(other: Calendar = Calendar.getInstance()): Int {
        return (this.diff(other) / 1000.0).roundToInt()
    }

    val Calendar.isPast: Boolean
        get() = this < Calendar.getInstance()

    fun Calendar.firstTimeOfDay() : Calendar {
        return (this.clone() as Calendar)
            .also {
                it[Calendar.HOUR_OF_DAY] = 0
                it[Calendar.MINUTE] = 0
                it[Calendar.SECOND] = 0
                it[Calendar.MILLISECOND] = 0
            }
    }

    class YearsAndDays (val years: Int, val days: Int)
    fun Calendar.diffYearsAndDays(other: Calendar): YearsAndDays {
        val d1p = this.firstTimeOfDay()
        val d2p = other.firstTimeOfDay()
        var currentYear = d1p[Calendar.YEAR]

        var diffDays = (d1p.diff(d2p) / 86400000.0).roundToInt()
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

        return YearsAndDays(diffYears, diffDays)
    }

    private fun getYearDays(year: Int): Int {
        return if (isLeapYear(year)) 366 else 365;
    }

    private fun isLeapYear(year: Int): Boolean {
        return year % 4 == 0 && year % 100 != 0 || year % 400 == 0
    }

    fun Calendar.getTimeString(): String {
        return parseCalendarToString(this, "HH:mm")
    }

    fun Calendar.getDateTimeString(): String {
        return parseCalendarToString(this, "yyyy-MM-dd HH:mm")
    }

    fun Calendar.getDateString(): String {
        return parseCalendarToString(this, "yyyy-MM-dd")
    }


    private fun parseCalendarToString(cal: Calendar, format: String): String {
        return SimpleDateFormat(format, Locale.getDefault()).format(cal.time)
    }

    fun Calendar.getLetterFrom(date: Calendar?): String {
        if (date == null || date.compareTo(DATE_ZERO) == 0) {
            return ""
        }

        return date.diffYearsAndDays(this).let {
            if (it.years == 0) {
                if (it.days == 0) "T"
                else "${it.days}D"
            } else {
                if (it.days == 0) "${it.years}Y"
                else "${it.years}Y${it.days}D"
            }
        }
    }
}