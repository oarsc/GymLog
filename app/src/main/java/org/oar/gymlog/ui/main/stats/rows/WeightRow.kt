package org.oar.gymlog.ui.main.stats.rows

import java.math.BigDecimal
import java.time.LocalDate

data class WeightRow(
    val day: LocalDate,
    val weight: BigDecimal? = null,
    val limitWeight: BigDecimal? = null,
    val manualWeight: BigDecimal? = null,
    val isBulkDay: Boolean? = null
) : IWeightRow