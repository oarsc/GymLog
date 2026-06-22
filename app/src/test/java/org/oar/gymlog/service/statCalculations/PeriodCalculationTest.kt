package org.oar.gymlog.service.statCalculations

import org.junit.Assert.assertEquals
import org.junit.Test
import org.oar.gymlog.model.WeightPeriod
import org.oar.gymlog.model.WeightPeriodModification
import org.oar.gymlog.service.PeriodCalculationService
import java.time.LocalDate

class PeriodCalculationTest {
    @Test
    fun `should calculate days correctly`() {
        val result = PeriodCalculationService(createWeightPeriod()).execute()

        assertEquals(731, result.days.size)
        assertEquals(643, result.bulkDays)
        assertEquals(LocalDate.of(2027, 5, 21), result.switchDate)
    }

    @Test
    fun `should not change bulkDays if modifications respect the default period values`() {
        val weightPeriod = createWeightPeriod().apply {
            modifications.add(
                    WeightPeriodModification(
                    initialDate = LocalDate.of(2025, 9, 16),
                    endDate = LocalDate.of(2025, 9, 26),
                    gramsPerWeek = -350,
                    weightPeriod = this
                )
            )
            modifications.add(
                WeightPeriodModification(
                    initialDate = LocalDate.of(2025, 10, 16),
                    endDate = LocalDate.of(2025, 10, 21),
                    gramsPerWeek = -350,
                    weightPeriod = this
                )
            )
        }

        val result = PeriodCalculationService(weightPeriod).execute()

        assertEquals(731, result.days.size)
        assertEquals(643, result.bulkDays)
        // 15 lean days have been advanced, so the lean period starts 15 days after
        assertEquals(LocalDate.of(2027, 6, 5), result.switchDate)
    }

    @Test
    fun `should delay switchDate after an aggressive cut month`() {
        val weightPeriod = createWeightPeriod().apply {
            modifications.add(
                WeightPeriodModification(
                    initialDate = LocalDate.of(2025, 9, 16),
                    endDate = LocalDate.of(2025, 10, 16),
                    gramsPerWeek = -1000,
                    weightPeriod = this
                )
            )
        }

        val result = PeriodCalculationService(weightPeriod).execute()

        assertEquals(731, result.days.size)
        assertEquals(688, result.bulkDays) // increases
        assertEquals(LocalDate.of(2027, 8, 4), result.switchDate)
    }

    fun createWeightPeriod(vararg modifications: WeightPeriodModification) = WeightPeriod(
        initialDate = LocalDate.of(2025, 8, 16),
        endDate = LocalDate.of(2027, 8, 16),
        initialWeight = "81.00".toBigDecimal(),
        gainGramsPerWeek = 80,
        lossGramsPerWeek = 350,
        expectedMuscleGain = "3.00".toBigDecimal(),
        toleranceGrams = 1000,
        modifications = modifications.toMutableList()
    )
}