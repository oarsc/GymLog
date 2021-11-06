package org.scp.gymlog.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Setter @Getter
public class Exercise {
	private final List<MuscularGroup> belongingGroups = new ArrayList<>();
	private String name;
}
