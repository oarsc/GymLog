package org.scp.gymlog.model;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import org.scp.gymlog.room.EntityMapped;
import org.scp.gymlog.room.entities.MuscleEntity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter @Getter
@AllArgsConstructor
public class Muscle implements EntityMapped<MuscleEntity> {
	private int id;
	@StringRes
	private int text;
	@DrawableRes
	private int icon;
	@ColorRes
	private int color;

	@Override
	public MuscleEntity toEntity() {
		MuscleEntity entity = new MuscleEntity();
		entity.muscleId = id;
		return entity;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Muscle fromEntity(@NonNull MuscleEntity entity) {
		id = entity.muscleId;
		return this;
	}
}
