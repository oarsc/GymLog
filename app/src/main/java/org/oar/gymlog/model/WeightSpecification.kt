package org.oar.gymlog.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import org.oar.gymlog.R
import org.oar.gymlog.util.Constants
import java.math.BigDecimal

enum class WeightSpecification(
    @DrawableRes val icon: Int,
    @StringRes val literal: Int,
    val weightAffectation: BigDecimal = BigDecimal.ONE
) {
    TOTAL_WEIGHT(
        icon = R.drawable.ic_asterisk_36x24dp,
        literal = R.string.weight_spec_total
    ),

    NO_BAR_WEIGHT(
        icon = R.drawable.ic_plates_36x24dp,
        literal = R.string.weight_spec_no_bar
    ),

    ONE_SIDE_WEIGHT(
        icon = R.drawable.ic_plate_36x24dp,
        literal = R.string.weight_spec_one_side,
        weightAffectation = Constants.HALF
    );
}