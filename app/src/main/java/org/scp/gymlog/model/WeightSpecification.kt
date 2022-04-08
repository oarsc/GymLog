package org.scp.gymlog.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import org.scp.gymlog.R

enum class WeightSpecification(
    @DrawableRes val icon: Int,
    @StringRes val literal: Int) {

    TOTAL_WEIGHT(
        R.drawable.ic_bar_with_plates_36x24dp,
        R.string.weight_spec_total),

    NO_BAR_WEIGHT(
        R.drawable.ic_plates_36x24dp,
        R.string.weight_spec_no_bar),

    ONE_SIDE_WEIGHT(
        R.drawable.ic_plate_36x24dp,
        R.string.weight_spec_one_side);
}