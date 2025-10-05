package org.oar.gymlog.ui.training.rows

import org.oar.gymlog.model.Bit

class TrainingBitRow(
    val bit: Bit
) : ITrainingBitRow {
    override val type = if (bit.superSet > 0)
        ITrainingBitRow.Type.BIT_SUPERSET
    else
        ITrainingBitRow.Type.BIT
}