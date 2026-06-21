package org.oar.gymlog.model

import org.oar.gymlog.room.EntityMappable
import org.oar.gymlog.room.entities.WeightPeriodEntity
import org.oar.gymlog.util.Constants.ONE_HUNDRED
import java.math.BigDecimal
import java.time.LocalDate

class WeightPeriod(
    var id: Int = 0,
    var initialDate: LocalDate = LocalDate.EPOCH,
    var endDate: LocalDate = LocalDate.EPOCH,
    var initialWeight: BigDecimal,
    var initialBodyFatPercent: BigDecimal,
    var gainGramsPerWeek: Int,
    var loseGramsPerWeek: Int,
    var expectedMuscleGain: BigDecimal,
    var modifications: MutableList<WeightPeriodModification> = mutableListOf()
) : EntityMappable<WeightPeriodEntity> {
    constructor(entity: WeightPeriodEntity): this(
        id = entity.weightPeriodId,
        initialDate = entity.start,
        endDate = entity.end,
        initialWeight = entity.initialWeight.toBigDecimal() / ONE_HUNDRED,
        initialBodyFatPercent = entity.initialBodyFatPercent.toBigDecimal() / ONE_HUNDRED,
        gainGramsPerWeek = entity.gainGramsPerWeek,
        loseGramsPerWeek = entity.loseGramsPerWeek,
        expectedMuscleGain = entity.expectedMuscleGain.toBigDecimal() / ONE_HUNDRED,
    )

    override fun toEntity(): WeightPeriodEntity = WeightPeriodEntity().apply {
        weightPeriodId = id
        initialDate = start
        endDate = end
        initialWeight = (this@WeightPeriod.initialWeight * ONE_HUNDRED).toInt()
        initialBodyFatPercent = (this@WeightPeriod.initialBodyFatPercent * ONE_HUNDRED).toInt()
        gainGramsPerWeek = this@WeightPeriod.gainGramsPerWeek
        loseGramsPerWeek = this@WeightPeriod.loseGramsPerWeek
        expectedMuscleGain = (this@WeightPeriod.expectedMuscleGain * ONE_HUNDRED).toInt()
    }
}
