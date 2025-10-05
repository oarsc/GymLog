package org.oar.gymlog.ui.main.history

import org.oar.gymlog.model.Muscle
import org.oar.gymlog.model.Training
import java.time.temporal.ChronoUnit

class TrainingData(
    val training: Training,
    val mostUsedMuscles: List<Muscle>
) {
    val duration: Int?
        get() = training.end?.let { ChronoUnit.MINUTES.between(training.start, it).toInt() }
}
