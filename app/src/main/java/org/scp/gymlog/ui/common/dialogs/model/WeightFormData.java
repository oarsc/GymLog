package org.scp.gymlog.ui.common.dialogs.model;

import org.scp.gymlog.model.Exercise;
import org.scp.gymlog.model.Weight;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class WeightFormData {
    private Weight weight;
    private Exercise exercise;
    private boolean exerciseUpdated;
}
