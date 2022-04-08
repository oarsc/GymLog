package org.scp.gymlog.ui.main.history

import org.scp.gymlog.model.Muscle
import java.util.*

class TrainingData(
    val id: Int,
    val startDate: Calendar,
    val mostUsedMuscles: List<Muscle>
)