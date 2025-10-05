package org.oar.gymlog.ui.common.dialogs.model

import org.oar.gymlog.model.Bar
import org.oar.gymlog.model.ExerciseType
import org.oar.gymlog.model.Weight
import org.oar.gymlog.model.WeightSpecification
import java.math.BigDecimal

class WeightFormData {
    var weight: Weight? = null
    var exerciseUpdated = false
    var step: BigDecimal? = null
    var bar: Bar? = null
    var type: ExerciseType = ExerciseType.NONE
    var weightSpec: WeightSpecification = WeightSpecification.TOTAL_WEIGHT
}
