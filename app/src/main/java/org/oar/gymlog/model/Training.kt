package org.oar.gymlog.model

import org.oar.gymlog.room.EntityMappable
import org.oar.gymlog.room.entities.TrainingEntity
import org.oar.gymlog.util.Data

class Training(
	entity: TrainingEntity
) : EntityMappable<TrainingEntity> {

	var id = entity.trainingId
	var gym = Data.getGym(entity.gymId)
	var start = entity.start
	var end = entity.end
	var note = entity.note

	override fun toEntity() = TrainingEntity().apply {
		trainingId = id
		gymId = gym.id
		start = this@Training.start
		end = this@Training.end
		note = this@Training.note
	}
}