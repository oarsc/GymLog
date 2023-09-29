package org.scp.gymlog.model

import org.scp.gymlog.room.EntityMappable
import org.scp.gymlog.room.entities.TrainingEntity
import org.scp.gymlog.util.Data

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