package org.oar.gymlog.service.statCalculations

import java.math.BigDecimal

data class DayInfo(
    val weight: BigDecimal,
    val limitWeight: BigDecimal,
    val isBulkDay: Boolean
)