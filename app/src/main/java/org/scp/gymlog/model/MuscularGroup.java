package org.scp.gymlog.model;

import org.scp.gymlog.room.EntityMapped;
import org.scp.gymlog.room.entities.MuscleEntity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter @Getter
@AllArgsConstructor
public class MuscularGroup implements EntityMapped<MuscleEntity> {
	private int id;
	private int text;
	private int icon;

	@Override
	public MuscleEntity toEntity() {
		MuscleEntity entity = new MuscleEntity();
		entity.muscleId = id;
		return entity;
	}

	@Override
	public void fromEntity(MuscleEntity entity) {
		id = entity.muscleId;
	}
}
