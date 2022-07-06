package org.scp.gymlog.ui.common.dialogs.model

import org.scp.gymlog.model.Bar
import org.scp.gymlog.model.Weight
import org.scp.gymlog.model.WeightSpecification
import java.math.BigDecimal

class WeightFormData {
    var weight: Weight? = null
    var exerciseUpdated = false
    var step: BigDecimal? = null
    var bar: Bar? = null
    var requiresBar = false
    var weightSpec: WeightSpecification = WeightSpecification.TOTAL_WEIGHT
}
