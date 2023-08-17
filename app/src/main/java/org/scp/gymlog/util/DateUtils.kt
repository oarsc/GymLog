package org.scp.gymlog.util

import org.scp.gymlog.util.Constants.DATE_ZERO
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.roundToInt

object DateUtils {

    fun currentDateTime() : LocalDateTime {
        return LocalDateTime.now(ZoneId.systemDefault())
    }

    val LocalDate.timeInMillis : Long
        get() = atStartOfDay().timeInMillis

    val LocalDateTime.timeInMillis : Long
        get() = atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    val Long.toLocalDateTime : LocalDateTime
        get() = Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDateTime()

    fun LocalDate.prevMonday() : LocalDate {
        if (dayOfWeek == DayOfWeek.MONDAY) {
            return this;
        }
        return plusDays((DayOfWeek.MONDAY.ordinal - dayOfWeek.ordinal).toLong())
    }

    val LocalDateTime.isPast: Boolean
        get() = this < currentDateTime()

    val LocalDateTime.isSet: Boolean
        get() = this > DATE_ZERO

    fun LocalDateTime.getTimeString(): String {
        return parseDateToString(this, "HH:mm")
    }

    fun LocalDateTime.getDateTimeString(): String {
        return parseDateToString(this, "yyyy-MM-dd HH:mm")
    }

    fun LocalDateTime.getDateString(): String {
        return parseDateToString(this, "yyyy-MM-dd")
    }

    fun Int.minutesToTimeString(): String {
        val hours = TimeUnit.MINUTES.toHours(this.toLong())
        val remainMinutes = this - TimeUnit.HOURS.toMinutes(hours)
        return String.format("%01d:%02d", hours, remainMinutes)
    }

    private fun parseDateToString(date: LocalDateTime, format: String): String {
        return date.format(DateTimeFormatter.ofPattern(format))
    }


    private fun getYearDays(year: Int): Int {
        return if (isLeapYear(year)) 366 else 365;
    }

    private fun isLeapYear(year: Int): Boolean {
        return year % 4 == 0 && year % 100 != 0 || year % 400 == 0
    }

    fun LocalDateTime.firstTimeOfDay() : LocalDateTime {
        return this.toLocalDate().atStartOfDay()
    }

    fun LocalDateTime.diff(other: LocalDateTime = currentDateTime()): Long {
        val duration = Duration.between(this, other)
        return abs(duration.toMillis())
    }

    fun LocalDateTime.diffSeconds(other: LocalDateTime = currentDateTime()): Int {
        return (this.diff(other) / 1000.0).roundToInt()
    }


    class YearsAndDays (val years: Int, val days: Int)
    fun LocalDateTime.diffYearsAndDays(other: LocalDateTime): YearsAndDays {
        val d1p = this.firstTimeOfDay()
        val d2p = other.firstTimeOfDay()
        var currentYear = d1p.year

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

    fun LocalDateTime.getLetterFrom(date: LocalDateTime?): String {
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