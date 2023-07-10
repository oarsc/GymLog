package org.scp.gymlog.ui.training.rows

interface ITrainingRow {
    enum class Type {
        VARIATION, HEADER, BIT, HEADER_SUPERSET, BIT_SUPERSET
    }
    val type: Type
}