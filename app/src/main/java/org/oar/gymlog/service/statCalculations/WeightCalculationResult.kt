package org.oar.gymlog.service.statCalculations

import org.oar.gymlog.model.WeightPeriod
import java.math.BigDecimal
import java.time.LocalDate

data class WeightCalculationResult(
    val weightPeriod: WeightPeriod,
    val days: Map<LocalDate, DayInfo>,
    val switchDate: LocalDate,
    val bulkDays: Int
)