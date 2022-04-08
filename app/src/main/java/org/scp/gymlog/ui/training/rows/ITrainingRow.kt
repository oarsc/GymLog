package org.scp.gymlog.ui.training.rows

interface ITrainingRow {
    enum class Type {
        VARIATION, HEADER, BIT
    }
    val type: Type
}