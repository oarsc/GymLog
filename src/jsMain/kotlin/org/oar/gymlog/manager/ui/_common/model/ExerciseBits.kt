package org.oar.gymlog.manager.ui._common.model

import org.oar.gymlog.manager.model.OutputBit
import org.oar.gymlog.manager.model.OutputExercise
import org.oar.gymlog.manager.model.OutputVariation

data class ExerciseBits(
    var exercise: OutputExercise,
    var variation: OutputVariation,
    var bits: MutableList<OutputBit> = mutableListOf()
)