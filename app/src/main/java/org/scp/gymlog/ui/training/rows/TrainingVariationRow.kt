package org.scp.gymlog.ui.training.rows

import org.scp.gymlog.model.Variation

class TrainingVariationRow(val variation: Variation?) : ITrainingRow {
    override val type: ITrainingRow.Type
        get() = ITrainingRow.Type.VARIATION
}