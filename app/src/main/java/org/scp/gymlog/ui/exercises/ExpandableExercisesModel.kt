package org.scp.gymlog.ui.exercises

import org.scp.gymlog.model.Variation

class ExpandableExercisesModel(
    val variation: Variation,
    val parent: ExpandableExercisesModel? = null
){
    val exercise = variation.exercise
    var expanded = false
    val canExpand
        get() = exercise.variations.size > 1
    val isChild: Boolean
        get() = parent != null
}