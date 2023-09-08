package org.scp.gymlog.ui.training.rows

class TrainingBitHeaderRow(
    superSet: Boolean = false
) : ITrainingBitRow {
    override val type = if (superSet)
            ITrainingBitRow.Type.HEADER_SUPERSET
        else
            ITrainingBitRow.Type.HEADER
}