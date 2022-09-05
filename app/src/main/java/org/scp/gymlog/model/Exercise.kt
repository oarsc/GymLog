package org.scp.gymlog.model

import org.scp.gymlog.exceptions.InternalException
import org.scp.gymlog.exceptions.SaveException
import org.scp.gymlog.room.EntityMappable
import org.scp.gymlog.room.entities.ExerciseEntity
import org.scp.gymlog.room.entities.ExerciseMuscleCrossRef
import org.scp.gymlog.room.entities.SecondaryExerciseMuscleCrossRef
import org.scp.gymlog.room.entities.VariationEntity
import org.scp.gymlog.util.Constants
import org.scp.gymlog.util.Data
import java.math.BigDecimal
import java.time.LocalDateTime

class Exercise() : EntityMappable<ExerciseEntity> {

	val primaryMuscles: MutableList<Muscle> = ArrayList()
	val secondaryMuscles: MutableList<Muscle> = ArrayList()
	val variations: MutableList<Variation> = ArrayList()
	var id = 0
	var name: String = ""
	var image: String = ""
	var lastTrained: LocalDateTime = Constants.DATE_ZERO

	val defaultVariation: Variation
		get() = variations.find { it.default }
			?: throw InternalException("Default variation not found for: $id")

	constructor(entity: ExerciseEntity) : this() {
		id = entity.exerciseId
		name = entity.name
		image = entity.image
		lastTrained = entity.lastTrained
	}

	override fun toEntity(): ExerciseEntity {
		if (name.isEmpty() || image.isEmpty()) {
			throw SaveException("Can't convert exercise to entity with empty name or image")
		}
		val entity = ExerciseEntity()
		entity.exerciseId = id
		entity.name = name
		entity.image = image
		entity.lastTrained = lastTrained

		defaultVariation.apply {
			entity.type = type
			entity.lastStep = step.multiply(Constants.ONE_HUNDRED).toInt()
			entity.lastWeightSpec = weightSpec
			entity.lastRestTime = restTime
			entity.lastBarId = bar?.id
		}
		return entity
	}

	fun toMuscleListEntities(): List<ExerciseMuscleCrossRef> {
		return primaryMuscles
			.map(Muscle::id)
			.map { muscleId: Int ->
				val xRef = ExerciseMuscleCrossRef()
				xRef.exerciseId = id
				xRef.muscleId = muscleId
				xRef
			}
	}

	fun toSecondaryMuscleListEntities(): List<SecondaryExerciseMuscleCrossRef> {
		return secondaryMuscles
			.map(Muscle::id)
			.map { muscleId: Int ->
				val xRef = SecondaryExerciseMuscleCrossRef()
				xRef.exerciseId = id
				xRef.muscleId = muscleId
				xRef
			}
	}

	fun toVariationListEntities(): List<VariationEntity> {
		return variations
			.map { it.toEntity().apply { exerciseId = this@Exercise.id } }
	}
}