package org.scp.gymlog.model

import org.scp.gymlog.room.EntityMappable
import org.scp.gymlog.room.entities.BitEntity
import org.scp.gymlog.util.Constants
import org.scp.gymlog.util.Data
import org.scp.gymlog.util.DateUtils.currentDateTime
import java.math.BigDecimal
import java.time.LocalDateTime

class Bit : EntityMappable<BitEntity> {

	val variation: Variation

	var id = 0
	var trainingId = 0
	var reps = 0
	var weight = Weight.INVALID
	var note: String = ""
	var timestamp: LocalDateTime
	var instant = false
	var gymId = 0
	var set = 0 // used in logRecyclerViewAdapter
	var superSet = 0

	init {
		this.timestamp = currentDateTime()
	}

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
		gymId = entity.gymId
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
		entity.gymId = gymId
		entity.totalWeight = weight.value.multiply(Constants.ONE_HUNDRED)?.toInt() ?: 0
		entity.superSet = superSet
		entity.kilos = weight.internationalSystem
		return entity
	}
}