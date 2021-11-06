package org.scp.gymlog.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

@Getter
public class Data {
	private final List<Exercise> exercises = new ArrayList<>();
	private final List<MuscularGroup> groups = new ArrayList<>();
	private final static Data instance = new Data();

	private Data(){}

	public static Data getInstance() {
		return instance;
	}
}
