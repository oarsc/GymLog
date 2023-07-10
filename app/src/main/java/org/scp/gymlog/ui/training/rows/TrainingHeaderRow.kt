package org.scp.gymlog.ui.training.rows

class TrainingHeaderRow(
    superSet: Boolean = false
) : ITrainingRow {
    override val type = if (superSet)
            ITrainingRow.Type.HEADER_SUPERSET
        else
            ITrainingRow.Type.HEADER
}