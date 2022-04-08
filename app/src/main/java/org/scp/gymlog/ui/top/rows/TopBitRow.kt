package org.scp.gymlog.ui.top.rows

import org.scp.gymlog.model.Bit

class TopBitRow(val bit: Bit) : ITopRow {
    override val type: ITopRow.Type
        get() = ITopRow.Type.BIT
}