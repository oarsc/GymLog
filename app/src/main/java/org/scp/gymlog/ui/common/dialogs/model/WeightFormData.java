package org.scp.gymlog.ui.common.dialogs.model;

import org.scp.gymlog.model.Bar;
import org.scp.gymlog.model.Weight;
import org.scp.gymlog.model.WeightSpecification;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class WeightFormData {
    private Weight weight;
    private boolean exerciseUpdated;

    private BigDecimal step;
    private Bar bar;
    private boolean requiresBar;
    private WeightSpecification weightSpec;
}
