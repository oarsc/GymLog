package org.oar.gymlog.model

import org.oar.gymlog.room.EntityMappable
import org.oar.gymlog.room.entities.GymEntity

class Gym(
	entity: GymEntity
) : EntityMappable<GymEntity> {

	var id = entity.gymId
	var name = entity.name

	override fun toEntity() = GymEntity(id, name)
}