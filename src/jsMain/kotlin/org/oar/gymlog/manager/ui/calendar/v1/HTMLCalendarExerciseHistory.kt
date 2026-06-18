package org.oar.gymlog.manager.ui.calendar.v1

import org.oar.gymlog.manager.model.Output
import org.oar.gymlog.manager.ui._common.HTMLCommonExerciseHistory
import org.oar.gymlog.manager.ui._common.model.ExerciseBits
import org.oar.gymlog.manager.ui.support.HTMLExerciseIcon
import org.oar.lib.HTMLDefinitionConstants.SPAN


class HTMLCalendarExerciseHistory(
    output: Output,
    private val bitGroup: ExerciseBits
): HTMLCommonExerciseHistory(output, bitGroup.bits) {
    override val columns = super.columns - Column.VARIATION

    init {
        +HTMLExerciseIcon(
            source = bitGroup.exercise.image,
            hue = output.primaries
                .first { it.exerciseId == bitGroup.exercise.exerciseId }
                .muscleId.let(output.muscles::get)!!
                .colorHue
        )
        +SPAN("variation-id-label") {
            -bitGroup.variation.variationId.toString()
        }
        +SPAN("exercise-label") {
            -bitGroup.exercise.name
        }
        if (!bitGroup.variation.def) {
            +SPAN("variation-label") {
                -bitGroup.variation.name
            }
        }
    }
}