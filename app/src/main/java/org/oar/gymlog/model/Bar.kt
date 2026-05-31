package org.oar.gymlog.model

import org.oar.gymlog.room.EntityMappable
import org.oar.gymlog.room.entities.BarEntity
import org.oar.gymlog.util.extensions.CommonExts.divideByHundred
import org.oar.gymlog.util.extensions.CommonExts.multiplyByHundred

data class Bar(
	val id: Int,
	val weight: Weight
) : EntityMappable<BarEntity> {
	constructor(entity: BarEntity): this(
		id = entity.barId,
		weight = Weight(
			entity.weight.divideByHundred(),
			entity.internationalSystem
		)
	)

	override fun toEntity(): BarEntity = BarEntity().apply {
		barId = id
		weight = this@Bar.weight.value.multiplyByHundred()
		internationalSystem = this@Bar.weight.internationalSystem
	}
}
