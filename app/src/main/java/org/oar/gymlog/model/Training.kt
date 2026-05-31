package org.oar.gymlog.model

import org.oar.gymlog.room.EntityMappable
import org.oar.gymlog.room.entities.TrainingEntity
import org.oar.gymlog.util.Data
import java.time.LocalDateTime

data class Training(
	var id: Int,
	var gym: Gym,
	var workout: Workout? = null,
	var start: LocalDateTime,
	var end: LocalDateTime?,
	var note: String,
) : EntityMappable<TrainingEntity> {
	constructor(entity: TrainingEntity) : this(
		id = entity.trainingId,
		gym = Data.getGym(entity.gymId),
		workout = entity.workoutId?.let(Data::getWorkout),
		start = entity.start,
		end = entity.end,
		note = entity.note,
	)

	override fun toEntity() = TrainingEntity().apply {
		trainingId = id
		gymId = gym.id
		workoutId = workout?.id
		start = this@Training.start
		end = this@Training.end
		note = this@Training.note
	}
}