package org.scp.gymlog.util;

import org.scp.gymlog.R;
import org.scp.gymlog.exceptions.LoadException;
import org.scp.gymlog.model.Bar;
import org.scp.gymlog.model.Exercise;
import org.scp.gymlog.model.Muscle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.Getter;

@Getter
public class Data {
	public static final int[] STEPS_KG = { 125, 250, 500, 1000, 1500, 2000, 2500 };

	private final List<Exercise> exercises = new ArrayList<>();
	private final List<Muscle> muscles = new ArrayList<>();
	private final List<Bar> bars = new ArrayList<>();
	private final static Data instance = new Data();
	private int trainingId = -1;

	private Data(){
		int muscleId = 0;
		muscles.addAll(Arrays.asList(
				new Muscle(++muscleId, R.string.group_pectoral, R.drawable.muscle_pectoral),
				new Muscle(++muscleId, R.string.group_upper_back, R.drawable.muscle_upper_back),
				new Muscle(++muscleId, R.string.group_lower_back, R.drawable.muscle_lower_back),
				new Muscle(++muscleId, R.string.group_deltoid, R.drawable.muscle_deltoid),
				new Muscle(++muscleId, R.string.group_trapezius, R.drawable.muscle_trapezius),
				new Muscle(++muscleId, R.string.group_biceps, R.drawable.muscle_biceps),
				new Muscle(++muscleId, R.string.group_triceps, R.drawable.muscle_triceps),
				new Muscle(++muscleId, R.string.group_forearm, R.drawable.muscle_forearm),
				new Muscle(++muscleId, R.string.group_quadriceps, R.drawable.muscle_quadriceps),
				new Muscle(++muscleId, R.string.group_hamstrings, R.drawable.muscle_hamstring),
				new Muscle(++muscleId, R.string.group_calves, R.drawable.muscle_calves),
				new Muscle(++muscleId, R.string.group_glutes, R.drawable.muscle_glutes),
				new Muscle(++muscleId, R.string.group_abdominals, R.drawable.muscle_abdominals),
				new Muscle(++muscleId, R.string.group_cardio, R.drawable.muscle_cardio)
		));
	}

	public static Data getInstance() {
		return instance;
	}

	public static Bar getBar(Data data, int barId) {
		return getInstance().bars.stream()
				.filter(bar -> bar.getId() == barId)
				.findFirst()
				.orElseThrow(() -> new LoadException("NO BAR FOUND id:"+barId));
	}

	public static Bar getBar(int exerciseId) {
		return getBar(getInstance(), exerciseId);
	}

	public static Exercise getExercise(Data data, int exerciseId) {
		return data.exercises.stream()
				.filter(exercise -> exercise.getId() == exerciseId)
				.findFirst()
				.orElseThrow(() -> new LoadException("NO EXERCISE FOUND id:"+exerciseId));
	}

	public static Exercise getExercise(int exerciseId) {
		return getExercise(getInstance(), exerciseId);
	}

	public static Muscle getMuscle(Data data, int muscleId) {
		return data.muscles.stream()
				.filter(muscle -> muscle.getId() == muscleId)
				.findFirst()
				.orElseThrow(() -> new LoadException("NO MUSCLE FOUND id:"+muscleId));
	}

	public static Muscle getMuscle(int muscleId) {
		return getMuscle(getInstance(), muscleId);
	}

	public void setTrainingId(int trainingId) {
		this.trainingId = trainingId;
	}
}
