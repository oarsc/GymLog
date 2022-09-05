package org.scp.gymlog.model

import org.scp.gymlog.room.EntityMappable
import org.scp.gymlog.room.entities.VariationEntity
import org.scp.gymlog.util.Constants
import java.math.BigDecimal
import java.time.LocalDateTime

class Variation(
	val exercise: Exercise
) : EntityMappable<VariationEntity>, Cloneable {

	var name = ""
	var id = 0
	var default = false

	var type: ExerciseType = ExerciseType.BARBELL
	var step: BigDecimal = Constants.FIVE
	var bar: Bar? = null
	var weightSpec = WeightSpecification.NO_BAR_WEIGHT
	var restTime: Int = -1

	constructor(entity: VariationEntity, exercise: Exercise) : this(exercise) {
		this.id = entity.variationId
		this.name = entity.name
	}

	override fun toEntity(): VariationEntity {
		val entity = VariationEntity()
		entity.variationId = id
		entity.name = name
		return entity
	}
}