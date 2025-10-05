package org.oar.gymlog.ui.top.rows

import org.oar.gymlog.model.Bit

class TopBitRow(val bit: Bit) : ITopRow {
    override val type: ITopRow.Type
        get() = ITopRow.Type.BIT
}