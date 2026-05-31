package org.oar.gymlog.model

import org.oar.gymlog.exceptions.SaveException
import org.oar.gymlog.room.EntityMappable
import org.oar.gymlog.room.entities.ExerciseEntity
import org.oar.gymlog.room.entities.ExerciseMuscleCrossRef
import org.oar.gymlog.room.entities.SecondaryExerciseMuscleCrossRef
import org.oar.gymlog.room.entities.VariationEntity
import org.oar.gymlog.util.Constants
import java.time.LocalDateTime

class Exercise(
	var id: Int = 0,
	var name: String = "",
	var image: String = "",
	var lastTrained: LocalDateTime = Constants.DATE_ZERO,
	val primaryMuscles: MutableList<Muscle> = mutableListOf(),
	val secondaryMuscles: MutableList<Muscle> = mutableListOf(),
	val variations: MutableList<Variation> = mutableListOf()
) : EntityMappable<ExerciseEntity> {
	constructor(entity: ExerciseEntity) : this(
		id = entity.exerciseId,
		name = entity.name,
		image = entity.image,
		lastTrained = entity.lastTrained,
	)

	override fun toEntity(): ExerciseEntity {
		if (name.isEmpty() || image.isEmpty()) {
			throw SaveException("Can't convert exercise to entity with empty name or image")
		}
		return ExerciseEntity().apply {
			exerciseId = id
			name = this@Exercise.name
			image = this@Exercise.image
			lastTrained = this@Exercise.lastTrained
		}
	}

	fun toMuscleListEntities(): List<ExerciseMuscleCrossRef> =
		primaryMuscles
			.map(Muscle::id)
			.map { muscleId: Int ->
				val xRef = ExerciseMuscleCrossRef()
				xRef.exerciseId = id
				xRef.muscleId = muscleId
				xRef
			}

	fun toSecondaryMuscleListEntities(): List<SecondaryExerciseMuscleCrossRef> =
		secondaryMuscles
			.map(Muscle::id)
			.map { muscleId: Int ->
				val xRef = SecondaryExerciseMuscleCrossRef()
				xRef.exerciseId = id
				xRef.muscleId = muscleId
				xRef
			}

	fun toVariationListEntities(): List<VariationEntity> = variations.map {
		it.toEntity()
			.apply { exerciseId = this@Exercise.id }
	}
}