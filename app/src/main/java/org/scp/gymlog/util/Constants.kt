package org.scp.gymlog.util

import java.math.BigDecimal
import java.util.*

object Constants {
    val TWO = BigDecimal("2")
    val FIVE = BigDecimal("5")
    val ONE_THOUSAND = BigDecimal("1000")
    val ONE_HUNDRED = BigDecimal("100")
    val LBS_RATIO = BigDecimal("2.2046226218488")
    val DATE_ZERO: Calendar = Calendar.getInstance()

    init {
        DATE_ZERO.timeInMillis = 0L
    }

    enum class IntentReference {
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