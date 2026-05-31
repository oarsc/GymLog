package org.oar.gymlog.model

import org.oar.gymlog.room.EntityMappable
import org.oar.gymlog.room.entities.WorkoutEntity

data class Workout(
	var id: Int = 0,
	var name: String = "",
	var exercises: List<WorkoutExercise> = emptyList()
) : EntityMappable<WorkoutEntity> {
	constructor(entity: WorkoutEntity): this(
		id = entity.workoutId,
		name = entity.name
	)

	override fun toEntity(): WorkoutEntity = WorkoutEntity().apply {
        workoutId = id
		name = this@Workout.name
    }
}
