package org.scp.gymlog.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import org.scp.gymlog.R

enum class ExerciseType(
    @DrawableRes val icon: Int,
    @StringRes val literal: Int,
    vararg weightModes: WeightSpecification,
    val defaultIndex: Int
) {
    NONE(
        R.drawable.ic_man_24dp,
        R.string.type_none,
        WeightSpecification.TOTAL_WEIGHT,
        defaultIndex = 0),

    DUMBBELL(
        R.drawable.ic_dumbbell_24dp,
        R.string.type_dumbbell,
        WeightSpecification.TOTAL_WEIGHT,
        defaultIndex = 0),

    BARBELL(
        R.drawable.ic_barbell_24dp,
        R.string.type_barbell,
        WeightSpecification.TOTAL_WEIGHT,
        WeightSpecification.NO_BAR_WEIGHT,
        WeightSpecification.ONE_SIDE_WEIGHT,
        defaultIndex = 0),

    PLATE(
        R.drawable.ic_plate_24dp,
        R.string.type_plate,
        WeightSpecification.TOTAL_WEIGHT,
        WeightSpecification.ONE_SIDE_WEIGHT,
        defaultIndex = 0),

    PULLEY_MACHINE(
        R.drawable.ic_pulley_24dp,
        R.string.type_pulley,
        WeightSpecification.TOTAL_WEIGHT,
        WeightSpecification.ONE_SIDE_WEIGHT,
        defaultIndex = 1),

    SMITH_MACHINE(
        R.drawable.ic_smith_24dp,
        R.string.type_smith,
        WeightSpecification.TOTAL_WEIGHT,
        WeightSpecification.ONE_SIDE_WEIGHT,
        defaultIndex = 0),

    MACHINE(
        R.drawable.ic_machine_24dp,
        R.string.type_machine,
        WeightSpecification.TOTAL_WEIGHT,
        WeightSpecification.ONE_SIDE_WEIGHT,
        defaultIndex = 0),

    CARDIO(
        R.drawable.ic_person_running_24dp,
        R.string.type_cardio,
        WeightSpecification.TOTAL_WEIGHT,
        defaultIndex = 0);
}