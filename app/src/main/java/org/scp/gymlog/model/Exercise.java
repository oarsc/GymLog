package org.scp.gymlog.model;

import static org.scp.gymlog.util.Constants.DATE_ZERO;
import static org.scp.gymlog.util.Constants.FIVE;
import static org.scp.gymlog.util.Constants.ONE_HUNDRED;

import androidx.annotation.NonNull;

import org.scp.gymlog.room.EntityMapped;
import org.scp.gymlog.room.entities.ExerciseEntity;
import org.scp.gymlog.room.entities.ExerciseMuscleCrossRef;
import org.scp.gymlog.room.entities.SecondaryExerciseMuscleCrossRef;
import org.scp.gymlog.room.entities.VariationEntity;
import org.scp.gymlog.util.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Setter @Getter
public class Exercise implements EntityMapped<ExerciseEntity> {
	private final List<Muscle> primaryMuscles = new ArrayList<>();
	private final List<Muscle> secondaryMuscles = new ArrayList<>();
	private final List<Variation> variations = new ArrayList<>();
	private int id;
	private String name;
	private String image;
	private Calendar lastTrained = DATE_ZERO;
	private BigDecimal step = FIVE;
	private boolean requiresBar;
	private Bar bar;
	private int restTime = -1;
	private WeightSpecification weightSpec = WeightSpecification.NO_BAR_WEIGHT;

	@Override
	public ExerciseEntity toEntity() {
		ExerciseEntity entity = new ExerciseEntity();
		entity.exerciseId = id;
		entity.name = name;
		entity.image = image;
		entity.lastTrained = lastTrained;
		entity.lastStep = step.multiply(ONE_HUNDRED).intValue();
		entity.requiresBar = requiresBar;
		entity.lastWeightSpec = weightSpec;
		entity.lastRestTime = restTime;
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
		step = BigDecimal.valueOf(entity.lastStep).divide(ONE_HUNDRED);
		requiresBar = entity.requiresBar;
		weightSpec = entity.lastWeightSpec;
		restTime = entity.lastRestTime;
		if (entity.lastBarId != null) {
			bar = Data.getBar(entity.lastBarId);
		}
		return this;
	}

	public ExerciseMuscleCrossRef[] toMuscleListEntities() {
		return primaryMuscles.stream()
				.map(Muscle::getId)
				.map(muscleId -> {
					ExerciseMuscleCrossRef xRef = new ExerciseMuscleCrossRef();
					xRef.exerciseId = id;
					xRef.muscleId = muscleId;
					return xRef;
				}).toArray(ExerciseMuscleCrossRef[]::new);
	}

	public SecondaryExerciseMuscleCrossRef[] toSecondaryMuscleListEntities() {
		return secondaryMuscles.stream()
				.map(Muscle::getId)
				.map(muscleId -> {
					SecondaryExerciseMuscleCrossRef xRef = new SecondaryExerciseMuscleCrossRef();
					xRef.exerciseId = id;
					xRef.muscleId = muscleId;
					return xRef;
				}).toArray(SecondaryExerciseMuscleCrossRef[]::new);
	}

	public VariationEntity[] toVariationListEntities() {
		return variations.stream()
				.map(v -> {
					VariationEntity variation = v.toEntity();
					variation.exerciseId = id;
					return variation;
				}).toArray(VariationEntity[]::new);
	}
}
