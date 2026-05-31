package org.oar.gymlog.model

import org.oar.gymlog.room.EntityMappable
import org.oar.gymlog.room.entities.WorkoutExerciseEntity

data class WorkoutExercise(
	var id: Int = 0,
	var sets: List<WorkoutSet> = emptyList(),
	var superSet: Int = 0,
	val workout: Workout,
	val variation: Variation
) : EntityMappable<WorkoutExerciseEntity> {
	constructor(entity: WorkoutExerciseEntity, workout: Workout, variation: Variation): this(
		id = entity.workoutExerciseId,
		superSet = entity.superSet,
		workout = workout,
		variation = variation
	)

	override fun toEntity(): WorkoutExerciseEntity = WorkoutExerciseEntity().apply {
		workoutExerciseId = id
		workoutId = workout.id
		variationId = variation.id
		superSet = this@WorkoutExercise.superSet
    }
}
