package org.scp.gymlog.ui.top.rows;

import org.scp.gymlog.model.Bit;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public class TopBitRow implements ITopRow {
    private final Bit bit;

    @Override
    public Type getType() {
        return Type.BIT;
    }
}
