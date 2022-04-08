package org.scp.gymlog.ui.top.rows

import org.scp.gymlog.model.Variation

class TopVariationRow(val variation: Variation) : ITopRow {
    override val type: ITopRow.Type
        get() = ITopRow.Type.VARIATION
}