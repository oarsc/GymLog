package org.scp.gymlog.model

import org.scp.gymlog.room.EntityMappable
import org.scp.gymlog.room.entities.GymEntity

class Gym(
	entity: GymEntity
) : EntityMappable<GymEntity> {

	var id = entity.gymId
	var name = entity.name

	override fun toEntity() = GymEntity(id, name)
}