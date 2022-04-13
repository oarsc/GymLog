package org.scp.gymlog.model

import org.scp.gymlog.room.EntityMappable
import org.scp.gymlog.room.entities.BitEntity
import org.scp.gymlog.util.Constants
import java.math.BigDecimal
import java.util.*

class Bit(val exerciseId: Int, val variationId: Int): EntityMappable<BitEntity> {

	var id = 0
	var trainingId = 0
	var reps = 0
	var weight = Weight.INVALID
	var note: String = ""
	var timestamp: Calendar = Calendar.getInstance()
	var instant = false
	var set = 0 // used in logRecyclerViewAdapter

	constructor(entity: BitEntity) : this(entity.exerciseId, entity.variationId ?: 0) {
		id = entity.bitId
		trainingId = entity.trainingId
		note = entity.note
		reps = entity.reps
		timestamp = entity.timestamp
		instant = entity.instant
		weight = Weight(
			BigDecimal.valueOf(entity.totalWeight.toLong()).divide(Constants.ONE_HUNDRED),
			entity.kilos
		)
	}

	override fun toEntity(): BitEntity {
		val entity = BitEntity()
		entity.bitId = id
		entity.exerciseId = exerciseId
		if (variationId > 0) {
			entity.variationId = variationId
		}
		entity.trainingId = trainingId
		entity.note = note
		entity.timestamp = timestamp
		entity.reps = reps
		entity.instant = instant
		entity.totalWeight = weight.value.multiply(Constants.ONE_HUNDRED)?.toInt() ?: 0
		entity.kilos = weight.internationalSystem
		return entity
	}
}