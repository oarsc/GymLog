package org.scp.gymlog.model;

import androidx.annotation.NonNull;

import org.scp.gymlog.room.EntityMapped;
import org.scp.gymlog.room.entities.ExerciseEntity;
import org.scp.gymlog.room.entities.ExerciseMuscleCrossRef;
import org.scp.gymlog.util.Data;

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
	private int step;
	private boolean requiresBar;
	private Bar bar;
	private WeightSpecification weightSpec = WeightSpecification.TOTAL_WEIGHT;

	@Override
	public ExerciseEntity toEntity() {
		ExerciseEntity entity = new ExerciseEntity();
		entity.exerciseId = id;
		entity.name = name;
		entity.image = image;
		entity.lastTrained = lastTrained;
		entity.lastStep = step;
		entity.requiresBar = requiresBar;
		entity.lastWeightSpec = weightSpec;
		if (bar != null) {
			entity.lastBarId = bar.getId();
		}
		return entity;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Exercise fromEntity(@NonNull ExerciseEntity entity) {
		id = entity.exerciseId;
		name = entity.name;
		image = entity.image;
		lastTrained = entity.lastTrained;
		step = entity.lastStep;
		requiresBar = entity.requiresBar;
		weightSpec = entity.lastWeightSpec;
		if (entity.lastBarId != null) {
			bar = Data.getBar(entity.lastBarId);
		}
		return this;
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
