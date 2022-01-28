package org.scp.gymlog.ui.top.rows;

import org.scp.gymlog.model.Variation;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public class TopVariationRow implements ITopRow {
    private final Variation variation;

    @Override
    public Type getType() {
        return Type.VARIATION;
    }
}
