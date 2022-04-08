package org.scp.gymlog.model

import org.scp.gymlog.room.EntityMappable
import org.scp.gymlog.room.entities.BarEntity
import org.scp.gymlog.util.Constants
import java.math.BigDecimal

class Bar : EntityMappable<BarEntity> {

	val id: Int
	val weight: Weight

	constructor(id: Int, weight: Weight) {
		this.id = id
		this.weight = weight
	}

	constructor(entity: BarEntity) {
		id = entity.barId
		weight = Weight(
			BigDecimal(entity.weight).divide(Constants.ONE_HUNDRED),
			entity.internationalSystem
		)
	}

	override fun toEntity(): BarEntity {
		val entity = BarEntity()
		entity.barId = id
		entity.weight = weight.value.multiply(Constants.ONE_HUNDRED).toInt()
		entity.internationalSystem = weight.internationalSystem
		return entity
	}
}
