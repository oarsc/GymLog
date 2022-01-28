package org.scp.gymlog.ui.training.rows;

public interface ITrainingRow {
    enum Type {
        VARIATION, HEADER, BIT
    }
    Type getType();
}
