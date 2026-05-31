package org.oar.gymlog.model

import org.oar.gymlog.room.EntityMappable
import org.oar.gymlog.room.entities.VariationEntity
import org.oar.gymlog.util.Constants
import org.oar.gymlog.util.Data
import org.oar.gymlog.util.extensions.CommonExts.divideByHundred
import org.oar.gymlog.util.extensions.CommonExts.multiplyByHundred
import java.math.BigDecimal

class Variation(
	var name: String = "",
	var id: Int = 0,
	var default: Boolean = false,
	var type: ExerciseType = ExerciseType.BARBELL,
	var gymId: Int? = null,
	var gymRelation: GymRelation = GymRelation.NO_RELATION,
	var step: BigDecimal = Constants.FIVE,
	var bar: Bar? = null,
	var weightSpec: WeightSpecification = WeightSpecification.TOTAL_WEIGHT,
	var restTime: Int = -1,
	val exercise: Exercise
) : EntityMappable<VariationEntity>, Cloneable {
	constructor(entity: VariationEntity, exercise: Exercise) : this(
		id = entity.variationId,
		name = entity.name,
		default = entity.def,
		type = entity.type,
		gymId = entity.gymId,
		gymRelation = entity.gymRelation,
		step = entity.lastStep.divideByHundred(),
		bar = entity.lastBarId?.let(Data::getBar),
		weightSpec = entity.lastWeightSpec,
		restTime = entity.lastRestTime,
		exercise = exercise
	)

	override fun toEntity(): VariationEntity = VariationEntity().apply {
		variationId = id
		name = if (default) "" else name
		def = default
		exerciseId = exercise.id
		type = this@Variation.type
		gymId = this@Variation.gymId
		gymRelation = this@Variation.gymRelation
		lastStep = step.multiplyByHundred()
		lastWeightSpec = weightSpec
		lastRestTime = restTime
		lastBarId = bar?.id
	}
}