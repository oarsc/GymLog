package org.oar.gymlog.model

import org.oar.gymlog.room.EntityMappable
import org.oar.gymlog.room.entities.GymEntity

data class Gym(
	var id: Int,
	var name: String,
) : EntityMappable<GymEntity> {
	constructor(entity: GymEntity) : this(
		id = entity.gymId,
		name = entity.name,
	)

	override fun toEntity() = GymEntity(id, name)
}