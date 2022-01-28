package org.scp.gymlog.ui.training;

import org.scp.gymlog.model.Exercise;
import org.scp.gymlog.ui.training.rows.ITrainingRow;

import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExerciseRows extends ArrayList<ITrainingRow> {
    private Exercise exercise;
    public ExerciseRows(Exercise exercise) {
        this.exercise = exercise;
    }
}
