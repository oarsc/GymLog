package org.oar.gymlog.util

import org.oar.gymlog.util.DateUtils.toLocalDateTime
import java.math.BigDecimal
import java.time.LocalDateTime

object Constants {
    val TWO = BigDecimal("2")
    val HALF = BigDecimal("0.5")
    val FIVE = BigDecimal("5")
    val ONE_THOUSAND = BigDecimal("1000")
    val ONE_HUNDRED = BigDecimal("100")
    val LBS_RATIO = BigDecimal("2.2046226218488")
    val DATE_ZERO: LocalDateTime = 0L.toLocalDateTime
    val TODAY by lazy { DateUtils.NOW }

    enum class IntentReference {
        NONE,
        REGISTRY,
        EXERCISE_LIST,
        SEARCH_LIST,
        CREATE_EXERCISE,
        CREATE_EXERCISE_FROM_MUSCLE,
        EDIT_EXERCISE,
        EDIT_VARIATION,
        CREATE_VARIATION,
        IMAGE_SELECTOR,
        TRAINING,
        TOP_RECORDS,

        EXPORT_FILE,
        IMPORT_FILE
    }

    object Dropbox {
        const val APP_KEY = "fts3973avw0k7hq"
        const val APP_SECRET = "hl64v4u7tz4ngwt"
    }
}