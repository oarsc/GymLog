package org.oar.gymlog.model

enum class TrainingOrder(val code: String) {
    CHRONOLOGICALLY("chronologically"),
    GROUP_EXERCISES("group_exercises");

    companion object {
        fun getByCode(code: String): TrainingOrder = TrainingOrder.entries
            .filter { order -> order.code == code }
            .getOrElse(0) {
                throw IllegalArgumentException("No order found for name \"$code\"") }
    }
}

