package org.oar.gymlog.service

import org.oar.gymlog.model.WeightPeriod
import org.oar.gymlog.model.WeightPeriodModification
import org.oar.gymlog.service.statCalculations.DayInfo
import org.oar.gymlog.service.statCalculations.WeightCalculationResult
import org.oar.gymlog.util.Constants
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class PeriodCalculationService(val weightPeriod: WeightPeriod) {
    private val totalDays = ChronoUnit.DAYS.between(weightPeriod.initialDate, weightPeriod.endDate).toInt()
    private val gainKgPerDay = weightPeriod.gainGramsPerWeek.toBigDecimal().bigScaled() / SEVEN / Constants.ONE_THOUSAND
    private val lossKgPerDay = weightPeriod.lossGramsPerWeek.toBigDecimal().bigScaled() / SEVEN / Constants.ONE_THOUSAND
    private val toleranceKg = weightPeriod.toleranceGrams.toBigDecimal() / Constants.ONE_THOUSAND

    fun execute(): WeightCalculationResult {
        val days = mutableMapOf<LocalDate, BigDecimal>()
        var pureBulkDays = weightPeriod.calculateBulkDays()
        var switchDate = weightPeriod.initialDate.plusDays(pureBulkDays.toLong())
        var bulkDaysCount = 0

        bulkDaysCount += days.fillPlannedDays(switchDate)
        weightPeriod.modifications.forEachIndexed { index, it ->
            bulkDaysCount += days.fillModificationDays(it)

            val alreadyPassedDays = ChronoUnit.DAYS.between(weightPeriod.initialDate, it.endDate).toInt()
            val extraBulkDays = weightPeriod.calculateBulkDays(
                totalDays = ChronoUnit.DAYS.between(it.endDate, weightPeriod.endDate).toInt(),
                initialWeight = days[it.endDate.minusDays(1)]!!
            ).coerceAtLeast(0)

            pureBulkDays = alreadyPassedDays + extraBulkDays
            switchDate = weightPeriod.initialDate.plusDays(pureBulkDays.toLong())

            bulkDaysCount += days.fillGapsBetween(
                switchDate = switchDate,
                fillUntil = weightPeriod.modifications.getOrNull(index + 1)?.initialDate ?: weightPeriod.endDate
            )
        }

        val daysInfo = days.mapValues { (_, weight) ->
            val scaledWeight = weight.setScale(2, RoundingMode.HALF_UP)
            DayInfo(
                weight = scaledWeight,
                limitWeight = scaledWeight + toleranceKg
            )
        }

        return WeightCalculationResult(
            weightPeriod = weightPeriod,
            days = daysInfo,
            maxWeight = daysInfo.values.maxOf { it.limitWeight },
            switchDate = switchDate,
            bulkDays = bulkDaysCount
        )
    }

    private fun MutableMap<LocalDate, BigDecimal>.fillPlannedDays(switchDate: LocalDate): Int {
        val endDate = weightPeriod.modifications.minOfOrNull(WeightPeriodModification::initialDate) ?: weightPeriod.endDate

        var currentDate = weightPeriod.initialDate
        var weight = weightPeriod.initialWeight
        var bulkDays = 0

        while (currentDate < endDate) {
            this[currentDate] = weight
            if (currentDate < switchDate) {
                weight += gainKgPerDay
                bulkDays++
            } else {
                weight -= lossKgPerDay
            }

            currentDate = currentDate.plusDays(1)
        }
        return bulkDays
    }

    private fun MutableMap<LocalDate, BigDecimal>.fillModificationDays(modification: WeightPeriodModification): Int {
        val kgPerDay = modification.gramsPerWeek.toBigDecimal().bigScaled() / SEVEN / Constants.ONE_THOUSAND

        var currentDate = modification.initialDate
        var weight = this[modification.initialDate.minusDays(1)]!! + kgPerDay
        var days = 0

        while (currentDate < modification.endDate) {
            this[currentDate] = weight
            weight += kgPerDay
            currentDate = currentDate.plusDays(1)
            days++
        }
        return if (modification.gramsPerWeek > 0) days else 0
    }

    private fun MutableMap<LocalDate, BigDecimal>.fillGapsBetween(
        switchDate: LocalDate,
        fillUntil: LocalDate,
    ): Int {
        var currentDate = this.keys.max()
        var weight = this[currentDate]!!
        var bulkDays = 0

        currentDate = currentDate.plusDays(1)
        while (currentDate < fillUntil) {
            if (currentDate < switchDate) {
                weight += gainKgPerDay
                bulkDays++
            } else {
                weight -= lossKgPerDay
            }
            this[currentDate] = weight

            currentDate = currentDate.plusDays(1)
        }
        return bulkDays
    }

    private fun WeightPeriod.calculateBulkDays(
        totalDays: Int = this@PeriodCalculationService.totalDays,
        initialWeight: BigDecimal = this.initialWeight
    ): Int {
        val initialY = this.initialWeight + expectedMuscleGain + totalDays.toBigDecimal() * lossKgPerDay
        val maxY = ((lossKgPerDay * initialWeight / gainKgPerDay) + initialY) / (BigDecimal.ONE + lossKgPerDay / gainKgPerDay)
        return ((maxY - initialWeight) / gainKgPerDay).setScale(0, RoundingMode.HALF_UP).toInt()
    }

    private fun BigDecimal.bigScaled() = setScale(BIG_SCALE)

    private companion object {
        private val SEVEN = 7.toBigDecimal()
        private const val BIG_SCALE = 15
    }
}