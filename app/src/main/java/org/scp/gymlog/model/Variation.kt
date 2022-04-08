package org.scp.gymlog.model

import org.scp.gymlog.room.EntityMappable
import org.scp.gymlog.room.entities.VariationEntity

class Variation(var name: String) : EntityMappable<VariationEntity>, Cloneable {
	var id: Int = 0

	constructor(id: Int, name: String) : this(name) {
		this.id = id
	}
	constructor(entity: VariationEntity) : this(entity.variationId, entity.name)

	override fun toEntity(): VariationEntity {
		val entity = VariationEntity()
		entity.variationId = id
		entity.name = name
		return entity
	}

	public override fun clone(): Variation {
		return Variation(id, name)
	}
}