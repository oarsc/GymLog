package org.scp.gymlog.ui.training.rows;

import org.scp.gymlog.model.Bit;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public class TrainingBitRow implements ITrainingRow {
    private final Bit bit;

    @Override
    public Type getType() {
        return Type.BIT;
    }
}
