package org.oar.gymlog.manager.utils

import kotlin.js.Date

object DateExt {
    fun Date.middleDate(other: Date): Date = Date((this.getTime() + other.getTime()) / 2)

    fun Date.toLocaleISOString(): String = buildString {
        append(toLocaleISODateString())
        append("T")
        append(toLocaleISOTimeString())
    }

    fun Date.toLocaleISODateString(): String {
        fun pad(n: Int, d: Int = 2): String = n.toString().padStart(d, '0')

        val year = getFullYear()
        val month = pad(getMonth() + 1)
        val day = pad(getDate())

        return "$year-$month-${day}"
    }

    fun Date.toLocaleISOTimeString(): String {
        fun pad(n: Int, d: Int = 2): String = n.toString().padStart(d, '0')

        val hours = pad(getHours())
        val minutes = pad(getMinutes())
        val seconds = pad(getSeconds())
        val millis = pad(getMilliseconds(), 3)

        return "$hours:$minutes:$seconds.$millis"
    }

    fun Date.startOfTheDay(): Date = Date.UTC(
        year = getUTCFullYear(),
        month = getUTCMonth(),
        day = getUTCDate(),
        hour = 0,
        minute = 0,
        second = 0,
        millisecond = 0
    ).let(::Date)

    operator fun Date.compareTo(other: Date): Int {
        val t1 = this.getTime()
        val t2 = other.getTime()
        return when {
            t1 < t2 -> -1
            t1 > t2 -> 1
            else -> 0
        }
    }
}