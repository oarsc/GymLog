package org.scp.gymlog.ui.main.history

import org.scp.gymlog.model.Muscle
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class TrainingData(
    val id: Int,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime?,
    val mostUsedMuscles: List<Muscle>
) {
    val duration: Int?
        get() = endTime?.let { ChronoUnit.MINUTES.between(startTime, it).toInt() }
}
