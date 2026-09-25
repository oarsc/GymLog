package org.oar.gymlog.model

import org.oar.gymlog.room.EntityMappable
import org.oar.gymlog.room.entities.WorkoutSetEntity
import org.oar.gymlog.util.extensions.CommonExts.divideByHundred
import org.oar.gymlog.util.extensions.CommonExts.multiplyByHundred

data class WorkoutSet(
	var id: Int = 0,
	var order: Int = 0,
	var reps: Int = 0,
	var note: String = "",
	var restTime: Int = -1,
	var warmUp: Boolean = false,
	val workoutExercise: WorkoutExercise,
) : EntityMappable<WorkoutSetEntity> {
	constructor(entity: WorkoutSetEntity, workoutExercise: WorkoutExercise): this(
		id = entity.workoutSetId,
		order = entity.order,
		reps = entity.reps,
		note = entity.note,
		restTime = entity.restTime,
		warmUp = entity.warmUp,
		workoutExercise = workoutExercise,
	)

	override fun toEntity(): WorkoutSetEntity = WorkoutSetEntity().apply {
		workoutSetId = id
		workoutExerciseId = workoutExercise.id
		order = this@WorkoutSet.order
		reps = this@WorkoutSet.reps
		note = this@WorkoutSet.note
		restTime = this@WorkoutSet.restTime
		warmUp = this@WorkoutSet.warmUp
	}
}
