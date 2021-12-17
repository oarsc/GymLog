package org.scp.gymlog.util;

import org.scp.gymlog.exceptions.LoadException;
import org.scp.gymlog.model.Bar;
import org.scp.gymlog.model.Exercise;
import org.scp.gymlog.model.Muscle;

import java.util.ArrayList;
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

	private Data(){}

	public static Data getInstance() {
		return instance;
	}

	public static Bar getBar(int barId) {
		return getInstance().bars.stream()
				.filter(bar -> bar.getId() == barId)
				.findFirst()
				.orElseThrow(() -> new LoadException("NO BAR FOUND id:"+barId));
	}

	public static Exercise getExercise(int exerciseId) {
		return getInstance().exercises.stream()
				.filter(exercise -> exercise.getId() == exerciseId)
				.findFirst()
				.orElseThrow(() -> new LoadException("NO EXERCISE FOUND id:"+exerciseId));
	}

	public void setTrainingId(int trainingId) {
		this.trainingId = trainingId;
	}
}
