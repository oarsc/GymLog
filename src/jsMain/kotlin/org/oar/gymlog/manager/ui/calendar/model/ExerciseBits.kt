package org.oar.gymlog.manager.ui.calendar.model

import org.oar.gymlog.manager.model.OutputBit
import org.oar.gymlog.manager.model.OutputExercise
import org.oar.gymlog.manager.model.OutputVariation

data class ExerciseBits(
    val exercise: OutputExercise,
    val variation: OutputVariation,
    val bits: MutableList<OutputBit> = mutableListOf()
)