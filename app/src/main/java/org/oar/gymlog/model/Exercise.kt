package org.oar.gymlog.model

import org.oar.gymlog.exceptions.InternalException
import org.oar.gymlog.exceptions.SaveException
import org.oar.gymlog.room.EntityMappable
import org.oar.gymlog.room.entities.ExerciseEntity
import org.oar.gymlog.room.entities.ExerciseMuscleCrossRef
import org.oar.gymlog.room.entities.SecondaryExerciseMuscleCrossRef
import org.oar.gymlog.room.entities.VariationEntity
import org.oar.gymlog.util.Constants
import org.oar.gymlog.util.Data
import java.time.LocalDateTime

class Exercise() : EntityMappable<ExerciseEntity> {

	val primaryMuscles = mutableListOf<Muscle>()
	val secondaryMuscles = mutableListOf<Muscle>()
	val variations = mutableListOf<Variation>()
	val gymVariations
		get() = variations.filter {
			it.gymRelation != GymRelation.STRICT_RELATION || it.gymId == Data.gym?.id
		}
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

	constructor(exercise: Exercise) : this() {
		primaryMuscles.addAll(exercise.primaryMuscles)
		secondaryMuscles.addAll(exercise.secondaryMuscles)

		exercise.variations
			.map { Variation(it, this) }
			.also { variations.addAll(it) }

		id = exercise.id
		name = exercise.name
		image = exercise.image
		lastTrained = exercise.lastTrained
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