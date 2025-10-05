package org.oar.gymlog.model

import org.oar.gymlog.room.EntityMappable
import org.oar.gymlog.room.entities.VariationEntity
import org.oar.gymlog.util.Constants
import org.oar.gymlog.util.Data
import java.math.BigDecimal

class Variation(
	val exercise: Exercise
) : EntityMappable<VariationEntity>, Cloneable {

	var name = ""
	var id = 0
	var default = false

	var type: ExerciseType = ExerciseType.BARBELL
	var gymId: Int? = null
	var gymRelation = GymRelation.NO_RELATION
	var step: BigDecimal = Constants.FIVE
	var bar: Bar? = null
	var weightSpec = WeightSpecification.TOTAL_WEIGHT
	var restTime: Int = -1

	constructor(variation: Variation, exercise: Exercise) : this(exercise) {
		name = variation.name
		id = variation.id
		default = variation.default

		type = variation.type
		gymId = variation.gymId
		gymRelation = variation.gymRelation
		step = variation.step
		bar = variation.bar
		weightSpec = variation.weightSpec
		restTime = variation.restTime
	}

	constructor(entity: VariationEntity, exercise: Exercise) : this(exercise) {
		this.id = entity.variationId
		this.name = entity.name
		this.default = entity.def

		this.type = entity.type
		this.gymId = entity.gymId
		this.gymRelation = entity.gymRelation
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
		entity.gymId = gymId
		entity.gymRelation = gymRelation
		entity.lastStep = step.multiply(Constants.ONE_HUNDRED).toInt()
		entity.lastWeightSpec = weightSpec
		entity.lastRestTime = restTime
		entity.lastBarId = bar?.id
		return entity
	}
}