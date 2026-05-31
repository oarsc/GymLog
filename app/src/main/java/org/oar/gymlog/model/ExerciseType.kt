package org.oar.gymlog.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import org.oar.gymlog.R

enum class ExerciseType(
    @DrawableRes val icon: Int,
    @StringRes val literal: Int,
    val weightModes: List<WeightSpecification>,
) {
    NONE(
        icon = R.drawable.ic_man_24dp,
        literal = R.string.type_none,
        weightModes = listOf(WeightSpecification.TOTAL_WEIGHT)
    ),

    DUMBBELL(
        icon = R.drawable.ic_dumbbell_24dp,
        literal = R.string.type_dumbbell,
        weightModes = listOf(WeightSpecification.TOTAL_WEIGHT)
    ),

    BARBELL(
        icon = R.drawable.ic_barbell_24dp,
        literal = R.string.type_barbell,
        weightModes = listOf(
            WeightSpecification.TOTAL_WEIGHT,
            WeightSpecification.NO_BAR_WEIGHT,
            WeightSpecification.ONE_SIDE_WEIGHT,
        )
    ),

    PLATE(
        icon = R.drawable.ic_plate_24dp,
        literal = R.string.type_plate,
        weightModes = listOf(WeightSpecification.TOTAL_WEIGHT)
    ),

    PULLEY_MACHINE(
        icon = R.drawable.ic_pulley_24dp,
        literal = R.string.type_pulley,
        weightModes = listOf(WeightSpecification.TOTAL_WEIGHT)
    ),

    SMITH_MACHINE(
        icon = R.drawable.ic_smith_24dp,
        literal = R.string.type_smith,
        weightModes = listOf(
            WeightSpecification.TOTAL_WEIGHT,
            WeightSpecification.ONE_SIDE_WEIGHT,
        )
    ),

    MACHINE(
        icon = R.drawable.ic_machine_24dp,
        literal = R.string.type_machine,
        weightModes = listOf(
            WeightSpecification.TOTAL_WEIGHT,
            WeightSpecification.ONE_SIDE_WEIGHT,
        )
    ),

    CARDIO(
        icon = R.drawable.ic_person_running_24dp,
        literal = R.string.type_cardio,
        weightModes = listOf(WeightSpecification.TOTAL_WEIGHT)
    );
}