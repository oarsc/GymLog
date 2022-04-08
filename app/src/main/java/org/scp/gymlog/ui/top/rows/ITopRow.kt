package org.scp.gymlog.ui.top.rows

interface ITopRow {
    enum class Type {
        VARIATION, HEADER, BIT, SPACE
    }
    val type: Type
}