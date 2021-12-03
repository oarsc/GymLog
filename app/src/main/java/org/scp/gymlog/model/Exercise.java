package org.scp.gymlog.model;

import org.scp.gymlog.room.EntityMapped;
import org.scp.gymlog.room.entities.ExerciseEntity;
import org.scp.gymlog.room.entities.ExerciseMuscleCrossRef;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Setter @Getter
public class Exercise implements EntityMapped<ExerciseEntity> {
	private final List<Muscle> belongingMuscles = new ArrayList<>();
	private int id;
	private String name;
	private String image;
	private Date lastTrained = new Date(0L);

	@Override
	public ExerciseEntity toEntity() {
		ExerciseEntity entity = new ExerciseEntity();
		entity.exerciseId = id;
		entity.name = name;
		entity.image = image;
		entity.lastTrained = lastTrained;
		return entity;
	}

	@Override
	public void fromEntity(ExerciseEntity entity) {
		id = entity.exerciseId;
		name = entity.name;
		image = entity.image;
		lastTrained = entity.lastTrained;
	}

	public ExerciseMuscleCrossRef[] toMuscleListEntities() {
		return belongingMuscles.stream()
				.map(Muscle::getId)
				.map(muscleId -> {
					ExerciseMuscleCrossRef xRef = new ExerciseMuscleCrossRef();
					xRef.exerciseId = id;
					xRef.muscleId = muscleId;
					return xRef;
				})
				.toArray(ExerciseMuscleCrossRef[]::new);
	}
}
