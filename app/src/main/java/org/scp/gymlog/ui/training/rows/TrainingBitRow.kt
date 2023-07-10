package org.scp.gymlog.ui.training.rows

import org.scp.gymlog.model.Bit

class TrainingBitRow(
    val bit: Bit
) : ITrainingRow {
    override val type = if (bit.superSet > 0)
        ITrainingRow.Type.BIT_SUPERSET
    else
        ITrainingRow.Type.BIT
}