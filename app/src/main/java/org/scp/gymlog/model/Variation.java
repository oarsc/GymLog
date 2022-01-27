package org.scp.gymlog.model;

import androidx.annotation.NonNull;

import org.scp.gymlog.room.EntityMapped;
import org.scp.gymlog.room.entities.VariationEntity;

import lombok.Getter;
import lombok.Setter;

@Setter @Getter
public class Variation implements EntityMapped<VariationEntity>, Cloneable {
	private int id;
	private String name;

	@Override
	public VariationEntity toEntity() {
		VariationEntity entity = new VariationEntity();
		entity.variationId = id;
		entity.name = name;
		return entity;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Variation fromEntity(@NonNull VariationEntity entity) {
		id = entity.variationId;
		name = entity.name;
		return this;
	}

	@NonNull
	@Override
	public Variation clone() {
		Variation variation = new Variation();
		variation.id = id;
		variation.name = name;
		return variation;
	}
}
