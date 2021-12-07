package org.scp.gymlog.model;

import org.scp.gymlog.room.EntityMapped;
import org.scp.gymlog.room.entities.BarEntity;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter @Getter
@AllArgsConstructor
public class Bar implements EntityMapped<BarEntity> {
	private final List<Muscle> belongingMuscles = new ArrayList<>();
	private int id;
	private int weight;
	private boolean internationalSystem;

	@Override
	public BarEntity toEntity() {
		BarEntity entity = new BarEntity();
		entity.barId = id;
		entity.weight = weight;
		entity.internationalSystem = internationalSystem;
		return entity;
	}

	@Override
	public Bar fromEntity(BarEntity entity) {
		id = entity.barId;
		weight = entity.weight;
		internationalSystem = entity.internationalSystem;
		return this;
	}
}
