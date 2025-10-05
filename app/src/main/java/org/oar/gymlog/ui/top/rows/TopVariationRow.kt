package org.oar.gymlog.ui.top.rows

import org.oar.gymlog.model.Variation

class TopVariationRow(val variation: Variation) : ITopRow {
    override val type: ITopRow.Type
        get() = ITopRow.Type.VARIATION
}