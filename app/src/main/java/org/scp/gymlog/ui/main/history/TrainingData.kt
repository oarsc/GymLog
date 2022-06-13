package org.scp.gymlog.ui.main.history

import org.scp.gymlog.model.Muscle
import java.time.LocalDateTime

class TrainingData(
    val id: Int,
    val startDate: LocalDateTime,
    val mostUsedMuscles: List<Muscle>
)