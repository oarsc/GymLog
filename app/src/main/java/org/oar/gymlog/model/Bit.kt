package org.oar.gymlog.model

import org.oar.gymlog.room.EntityMappable
import org.oar.gymlog.room.entities.BitEntity
import org.oar.gymlog.util.Data
import org.oar.gymlog.util.DateUtils.NOW
import org.oar.gymlog.util.extensions.CommonExts.divideByHundred
import org.oar.gymlog.util.extensions.CommonExts.multiplyByHundred
import java.time.LocalDateTime

class Bit(
	val variation: Variation,
	var id: Int = 0,
	var trainingId: Int = 0,
	var reps: Int = 0,
	var weight: Weight = Weight.INVALID,
	var note: String = "",
	var timestamp: LocalDateTime = NOW,
	var instant: Boolean = false,
	var superSet: Int = 0,
) : EntityMappable<BitEntity> {
	var set = 0 // used in logRecyclerViewAdapter

	constructor(entity: BitEntity): this(
		variation = Data.getVariation(entity.variationId),
		id = entity.bitId,
		trainingId = entity.trainingId,
		note = entity.note,
		reps = entity.reps,
		timestamp = entity.timestamp,
		instant = entity.instant,
		superSet = entity.superSet,
		weight = Weight(
            value = entity.totalWeight.divideByHundred(),
            internationalSystem = entity.kilos
		)
	)

	override fun toEntity(): BitEntity = BitEntity().apply {
		bitId = id
		variationId = variation.id
		trainingId = this@Bit.trainingId
		note = this@Bit.note
		timestamp = this@Bit.timestamp
		reps = this@Bit.reps
		instant = this@Bit.instant
		totalWeight = weight.value.multiplyByHundred()
		superSet = this@Bit.superSet
		kilos = weight.internationalSystem
	}
}