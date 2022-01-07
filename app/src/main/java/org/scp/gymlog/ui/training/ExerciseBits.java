package org.scp.gymlog.ui.training;

import org.scp.gymlog.model.Bit;
import org.scp.gymlog.model.Exercise;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExerciseBits {
    private Exercise exercise;
    private List<Bit> bits = new ArrayList<>();

    public ExerciseBits(Exercise exercise) {
        this.exercise = exercise;
    }
}
