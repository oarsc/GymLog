package org.scp.gymlog.model

import org.scp.gymlog.room.EntityMappable
import org.scp.gymlog.room.entities.VariationEntity
import org.scp.gymlog.util.Constants
import org.scp.gymlog.util.Data
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
		this.default = entity.def

		this.type = entity.type
		this.step = BigDecimal.valueOf(entity.lastStep.toLong()).divide(Constants.ONE_HUNDRED)
		this.weightSpec = entity.lastWeightSpec
		this.restTime = entity.lastRestTime
		if (entity.lastBarId != null) {
			bar = Data.getBar(entity.lastBarId!!)
		}
	}

	override fun toEntity(): VariationEntity {
		val entity = VariationEntity()
		entity.variationId = id
		entity.name = if (default) "" else name
		entity.def = default
		entity.exerciseId = exercise.id
		entity.type = type
		entity.lastStep = step.multiply(Constants.ONE_HUNDRED).toInt()
		entity.lastWeightSpec = weightSpec
		entity.lastRestTime = restTime
		entity.lastBarId = bar?.id
		return entity
	}
}