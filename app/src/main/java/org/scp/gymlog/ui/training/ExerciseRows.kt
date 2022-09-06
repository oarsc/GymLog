package org.scp.gymlog.ui.training

import org.scp.gymlog.model.Exercise
import org.scp.gymlog.ui.training.rows.ITrainingRow

class ExerciseRows(val exercise: Exercise): MutableList<ITrainingRow> by mutableListOf()