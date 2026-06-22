package org.oar.gymlog.model

import org.oar.gymlog.room.EntityMappable
import org.oar.gymlog.room.entities.WeightPeriodEntity
import org.oar.gymlog.util.Constants.ONE_HUNDRED
import org.oar.gymlog.util.Constants.ONE_THOUSAND
import java.math.BigDecimal
import java.time.LocalDate

class WeightPeriod(
    var id: Int = 0,
    var initialDate: LocalDate = LocalDate.EPOCH,
    var endDate: LocalDate = LocalDate.EPOCH,
    var initialWeight: BigDecimal,
    var gainGramsPerWeek: Int,
    var lossGramsPerWeek: Int,
    var expectedMuscleGain: BigDecimal,
    var modifications: MutableList<WeightPeriodModification> = mutableListOf(),
    var toleranceGrams: Int = 0
) : EntityMappable<WeightPeriodEntity> {
    constructor(entity: WeightPeriodEntity): this(
        id = entity.weightPeriodId,
        initialDate = entity.start,
        endDate = entity.end,
        initialWeight = entity.initialWeight.toBigDecimal() / ONE_HUNDRED,
        gainGramsPerWeek = entity.gainGramsPerWeek,
        lossGramsPerWeek = entity.lossGramsPerWeek,
        expectedMuscleGain = entity.expectedMuscleGain.toBigDecimal() / ONE_THOUSAND,
        toleranceGrams = entity.toleranceGrams,
    )

    override fun toEntity(): WeightPeriodEntity = WeightPeriodEntity().apply {
        weightPeriodId = id
        start = initialDate
        end = endDate
        initialWeight = (this@WeightPeriod.initialWeight * ONE_HUNDRED).toInt()
        gainGramsPerWeek = this@WeightPeriod.gainGramsPerWeek
        lossGramsPerWeek = this@WeightPeriod.lossGramsPerWeek
        expectedMuscleGain = (this@WeightPeriod.expectedMuscleGain * ONE_THOUSAND).toInt()
        toleranceGrams = this@WeightPeriod.toleranceGrams
    }
}
