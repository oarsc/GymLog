package org.scp.gymlog.model;

import org.scp.gymlog.room.EntityMapped;
import org.scp.gymlog.room.entities.ExerciseEntity;
import org.scp.gymlog.room.entities.ExerciseMuscleCrossRef;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Setter @Getter
public class Exercise implements EntityMapped<ExerciseEntity> {
	private final List<MuscularGroup> belongingMuscularGroups = new ArrayList<>();
	private int id;
	private String name;
	private String image;

	@Override
	public ExerciseEntity toEntity() {
		ExerciseEntity entity = new ExerciseEntity();
		entity.exerciseId = id;
		entity.name = name;
		entity.image = image;
		return entity;
	}

	@Override
	public void fromEntity(ExerciseEntity entity) {
		id = entity.exerciseId;
		name = entity.name;
		image = entity.image;
	}

	public ExerciseMuscleCrossRef[] toMuscleListEntities() {
		return belongingMuscularGroups.stream()
				.map(MuscularGroup::getId)
				.map(groupId -> {
					ExerciseMuscleCrossRef xRef = new ExerciseMuscleCrossRef();
					xRef.exerciseId = id;
					xRef.muscleId = groupId;
					return xRef;
				})
				.toArray(ExerciseMuscleCrossRef[]::new);
	}
}
