package org.scp.gymlog.ui.main.history;

import org.scp.gymlog.model.Muscle;

import java.util.Calendar;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TrainingData {
    private List<Muscle> mostUsedMuscles;
    private Calendar startDate;
    private int id;
}