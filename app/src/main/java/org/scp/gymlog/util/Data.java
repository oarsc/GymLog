package org.scp.gymlog.util;

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
	private final static Data instance = new Data();

	private Data(){}

	public static Data getInstance() {
		return instance;
	}
}
