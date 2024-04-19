package org.scp.gymlog.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import org.scp.gymlog.R

enum class ExerciseType(
    @DrawableRes val icon: Int,
    @StringRes val literal: Int,
    val weightModes: List<WeightSpecification>,
) {
    NONE(
        R.drawable.ic_man_24dp,
        R.string.type_none,
        listOf(WeightSpecification.TOTAL_WEIGHT)
    ),

    DUMBBELL(
        R.drawable.ic_dumbbell_24dp,
        R.string.type_dumbbell,
        listOf(WeightSpecification.TOTAL_WEIGHT)
    ),

    BARBELL(
        R.drawable.ic_barbell_24dp,
        R.string.type_barbell,
        listOf(
            WeightSpecification.TOTAL_WEIGHT,
            WeightSpecification.NO_BAR_WEIGHT,
            WeightSpecification.ONE_SIDE_WEIGHT,
        )
    ),

    PLATE(
        R.drawable.ic_plate_24dp,
        R.string.type_plate,
        listOf(WeightSpecification.TOTAL_WEIGHT)
    ),

    PULLEY_MACHINE(
        R.drawable.ic_pulley_24dp,
        R.string.type_pulley,
        listOf(WeightSpecification.TOTAL_WEIGHT)
    ),

    SMITH_MACHINE(
        R.drawable.ic_smith_24dp,
        R.string.type_smith,
        listOf(
            WeightSpecification.TOTAL_WEIGHT,
            WeightSpecification.ONE_SIDE_WEIGHT,
        )
    ),

    MACHINE(
        R.drawable.ic_machine_24dp,
        R.string.type_machine,
        listOf(
            WeightSpecification.TOTAL_WEIGHT,
            WeightSpecification.ONE_SIDE_WEIGHT,
        )
    ),

    CARDIO(
        R.drawable.ic_person_running_24dp,
        R.string.type_cardio,
        listOf(WeightSpecification.TOTAL_WEIGHT)
    );
}