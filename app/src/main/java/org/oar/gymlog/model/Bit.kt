package org.oar.gymlog.model

import org.oar.gymlog.room.EntityMappable
import org.oar.gymlog.room.entities.BitEntity
import org.oar.gymlog.util.Constants
import org.oar.gymlog.util.Data
import org.oar.gymlog.util.DateUtils.NOW
import java.math.BigDecimal
import java.time.LocalDateTime

class Bit : EntityMappable<BitEntity> {

	val variation: Variation

	var id = 0
	var trainingId = 0
	var reps = 0
	var weight = Weight.INVALID
	var note: String = ""
	var timestamp: LocalDateTime = NOW
	var instant = false
	var set = 0 // used in logRecyclerViewAdapter
	var superSet = 0

	constructor(variation: Variation) {
		this.variation = variation
	}

	constructor(entity: BitEntity) {
		variation = Data.getVariation(entity.variationId)

		id = entity.bitId
		trainingId = entity.trainingId
		note = entity.note
		reps = entity.reps
		timestamp = entity.timestamp
		instant = entity.instant
		superSet = entity.superSet
		weight = Weight(
			BigDecimal.valueOf(entity.totalWeight.toLong()).divide(Constants.ONE_HUNDRED),
			entity.kilos
		)
	}

	override fun toEntity(): BitEntity {
		val entity = BitEntity()
		entity.bitId = id
		entity.variationId = variation.id
		entity.trainingId = trainingId
		entity.note = note
		entity.timestamp = timestamp
		entity.reps = reps
		entity.instant = instant
		entity.totalWeight = weight.value.multiply(Constants.ONE_HUNDRED)?.toInt() ?: 0
		entity.superSet = superSet
		entity.kilos = weight.internationalSystem
		return entity
	}
}