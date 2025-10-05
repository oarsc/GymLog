package org.oar.gymlog.util

import org.oar.gymlog.util.Constants.DATE_ZERO
import java.time.DayOfWeek
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.roundToInt

object DateUtils {
    val NOW: LocalDateTime
        get() = LocalDateTime.now(ZoneId.systemDefault())

    val LocalDate.timeInMillis : Long
        get() = atStartOfDay().timeInMillis

    val LocalDateTime.timeInMillis : Long
        get() = atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    val Long.toLocalDateTime : LocalDateTime
        get() = Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDateTime()

    fun LocalDate.previousMonday() : LocalDate =
        if (dayOfWeek == DayOfWeek.MONDAY) this
        else plusDays((DayOfWeek.MONDAY.ordinal - dayOfWeek.ordinal).toLong())

    val Long.isSystemTimePast: Boolean
        get() = this < System.currentTimeMillis()

    fun LocalDateTime.getTimeString(): String = parseDateToString("HH:mm")
    fun LocalDateTime.getDateTimeString(): String = parseDateToString("yyyy-MM-dd HH:mm")
    fun LocalDateTime.getDateString(): String = parseDateToString("yyyy-MM-dd")
    fun LocalDateTime.getTimestampString(): String = parseDateToString("yyyyMMdd-HHmmss")

    private fun LocalDateTime.parseDateToString(format: String): String =
        format(DateTimeFormatter.ofPattern(format))

    fun Int.minutesToTimeString(): String {
        val hours = TimeUnit.MINUTES.toHours(this.toLong())
        val remainMinutes = this - TimeUnit.HOURS.toMinutes(hours)
        return String.format("%01d:%02d", hours, remainMinutes)
    }

    private fun getYearDays(year: Int): Int =
        if (isLeapYear(year)) 366 else 365

    private fun isLeapYear(year: Int): Boolean =
        year % 4 == 0 && year % 100 != 0 || year % 400 == 0

    fun LocalDateTime.firstTimeOfDay() : LocalDateTime =
        toLocalDate().atStartOfDay()

    fun Long.diff(other: Long = System.currentTimeMillis()): Long = abs(this - other)
    fun Long.diffSeconds(millis: Long = System.currentTimeMillis()): Int = (diff(millis) / 1000.0).roundToInt()

    class YearsAndDays(val years: Int, val days: Int)
    fun LocalDateTime.diffYearsAndDays(other: LocalDateTime): YearsAndDays {
        val d1p = this.firstTimeOfDay()
        val d2p = other.firstTimeOfDay()
        var currentYear = d1p.year

        val diff = abs(Duration.between(d1p, d2p).toMillis())
        var diffDays = (diff / 86400000.0).roundToInt()
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