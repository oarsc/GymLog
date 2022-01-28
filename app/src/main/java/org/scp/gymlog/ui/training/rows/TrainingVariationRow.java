package org.scp.gymlog.ui.training.rows;

import org.scp.gymlog.model.Variation;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public class TrainingVariationRow implements ITrainingRow {
    private final Variation variation;

    @Override
    public Type getType() {
        return Type.VARIATION;
    }
}
