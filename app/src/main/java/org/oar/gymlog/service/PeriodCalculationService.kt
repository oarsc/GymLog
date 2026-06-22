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
        val days = mutableMapOf<LocalDate, DayValue>()
        var pureBulkDays = weightPeriod.calculateBulkDays()
        var switchDate = weightPeriod.initialDate.plusDays(pureBulkDays.toLong())
        var bulkDaysCount = 0

        bulkDaysCount += days.fillPlannedDays(switchDate)
        weightPeriod.modifications.forEachIndexed { index, it ->
            bulkDaysCount += days.fillModificationDays(it)

            val alreadyPassedDays = ChronoUnit.DAYS.between(weightPeriod.initialDate, it.endDate).toInt()
            val extraBulkDays = weightPeriod.calculateBulkDays(
                totalDays = ChronoUnit.DAYS.between(it.endDate, weightPeriod.endDate).toInt(),
                initialWeight = days[it.endDate.minusDays(1)]!!.weight
            ).coerceAtLeast(0)

            pureBulkDays = alreadyPassedDays + extraBulkDays
            switchDate = weightPeriod.initialDate.plusDays(pureBulkDays.toLong())

            bulkDaysCount += days.fillGapsBetween(
                switchDate = switchDate,
                fillUntil = weightPeriod.modifications.getOrNull(index + 1)?.initialDate ?: weightPeriod.endDate
            )
        }

        return WeightCalculationResult(
            weightPeriod = weightPeriod,
            days = days.mapToDayInfo(),
            switchDate = switchDate,
            bulkDays = bulkDaysCount
        )
    }

    private fun MutableMap<LocalDate, DayValue>.fillPlannedDays(switchDate: LocalDate): Int {
        val endDate = weightPeriod.modifications.minOfOrNull(WeightPeriodModification::initialDate) ?: weightPeriod.endDate

        var currentDate = weightPeriod.initialDate
        var weight = weightPeriod.initialWeight
        var bulkDays = 0

        while (currentDate < endDate) {
            val isBulkDay = currentDate < switchDate
            if (isBulkDay) {
                weight += gainKgPerDay
                bulkDays++
            } else {
                weight -= lossKgPerDay
            }
            this[currentDate] = DayValue(
                weight = weight,
                isBulkDay = isBulkDay
            )

            currentDate = currentDate.plusDays(1)
        }
        return bulkDays
    }

    private fun MutableMap<LocalDate, DayValue>.fillModificationDays(modification: WeightPeriodModification): Int {
        val bulking = modification.gramsPerWeek > 0
        val kgPerDay = modification.gramsPerWeek.toBigDecimal().bigScaled() / SEVEN / Constants.ONE_THOUSAND

        var currentDate: LocalDate
        var weight: BigDecimal
        if (modification.initialDate == weightPeriod.initialDate) {
            currentDate = weightPeriod.initialDate
            weight = weightPeriod.initialWeight
        } else {
            currentDate = modification.initialDate
            weight = this[modification.initialDate.minusDays(1)]!!.weight
        }

        var days = 0

        while (currentDate < modification.endDate) {
            weight += kgPerDay
            this[currentDate] = DayValue(
                weight = weight,
                isBulkDay = bulking
            )
            currentDate = currentDate.plusDays(1)
            days++
        }
        return if (bulking) days else 0
    }

    private fun MutableMap<LocalDate, DayValue>.fillGapsBetween(
        switchDate: LocalDate,
        fillUntil: LocalDate,
    ): Int {
        var currentDate = this.keys.max()
        var weight = this[currentDate]!!.weight
        var bulkDays = 0

        currentDate = currentDate.plusDays(1)
        while (currentDate < fillUntil) {
            val isBulkDay = currentDate < switchDate
            if (isBulkDay) {
                weight += gainKgPerDay
                bulkDays++
            } else {
                weight -= lossKgPerDay
            }
            this[currentDate] = DayValue(
                weight = weight,
                isBulkDay = isBulkDay
            )

            currentDate = currentDate.plusDays(1)
        }
        return bulkDays
    }

    private fun Map<LocalDate, DayValue>.mapToDayInfo(): Map<LocalDate, DayInfo> = buildMap {
        var prevDay: DayValue? = null
        var currentDate = weightPeriod.initialDate

        while (currentDate <= weightPeriod.endDate) {
            val currentDay = this@mapToDayInfo[currentDate]

            val weight = (prevDay?.weight ?: weightPeriod.initialWeight).setScale(2, RoundingMode.HALF_UP)
            this[currentDate] = DayInfo(
                weight = weight,
                limitWeight = weight + toleranceKg,
                isBulkDay = currentDay?.isBulkDay ?: prevDay?.isBulkDay!!
            )

            prevDay = currentDay
            currentDate = currentDate.plusDays(1)
        }



        mapValues { (_, dayValue) ->
            val scaledWeight = dayValue.weight.setScale(2, RoundingMode.HALF_UP)
            DayInfo(
                weight = scaledWeight,
                limitWeight = scaledWeight + toleranceKg,
                isBulkDay = dayValue.isBulkDay
            )
        }
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

    data class DayValue(
        // This weight represents the value at the end of the day
        val weight: BigDecimal,
        val isBulkDay: Boolean
    )

    private companion object {
        private val SEVEN = 7.toBigDecimal()
        private const val BIG_SCALE = 15
    }
}