package org.oar.gymlog.ui.training.rows

interface ITrainingBitRow {
    enum class Type {
        HEADER, BIT, HEADER_SUPERSET, BIT_SUPERSET
    }
    val type: Type
}