package org.scp.gymlog.ui.training.rows

import org.scp.gymlog.model.Bit

class TrainingBitRow(
    val bit: Bit
) : ITrainingBitRow {
    override val type = if (bit.superSet > 0)
        ITrainingBitRow.Type.BIT_SUPERSET
    else
        ITrainingBitRow.Type.BIT
}