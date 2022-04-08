package org.scp.gymlog.ui.training.rows

import org.scp.gymlog.model.Bit

class TrainingBitRow(val bit: Bit) : ITrainingRow {
    override val type: ITrainingRow.Type
        get() = ITrainingRow.Type.BIT
}