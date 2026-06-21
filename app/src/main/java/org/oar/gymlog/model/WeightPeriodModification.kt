package org.oar.gymlog.model

import org.oar.gymlog.room.EntityMappable
import org.oar.gymlog.room.entities.WeightPeriodModificationEntity
import org.oar.gymlog.util.Constants.ONE_THOUSAND
import java.math.BigDecimal
import java.time.LocalDate

class WeightPeriodModification(
    var id: Int = 0,
    var initialDate: LocalDate = LocalDate.EPOCH,
    var endDate: LocalDate = LocalDate.EPOCH,
    var gramsPerWeek: BigDecimal,
    val weightPeriod: WeightPeriod
) : EntityMappable<WeightPeriodModificationEntity> {
    constructor(entity: WeightPeriodModificationEntity, weightPeriod: WeightPeriod): this(
        id = entity.weightPeriodModificationId,
        initialDate = entity.start,
        endDate = entity.end,
        gramsPerWeek = entity.gramsPerWeek.toBigDecimal() / ONE_THOUSAND,
        weightPeriod = weightPeriod
    )

    override fun toEntity(): WeightPeriodModificationEntity = WeightPeriodModificationEntity().apply {
        weightPeriodModificationId = id
        weightPeriodId = weightPeriod.id
        start = initialDate
        end = endDate
        gramsPerWeek = (this@WeightPeriodModification.gramsPerWeek * ONE_THOUSAND).toInt()
    }
}
