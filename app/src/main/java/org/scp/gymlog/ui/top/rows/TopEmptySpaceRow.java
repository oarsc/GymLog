package org.scp.gymlog.ui.top.rows;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public class TopEmptySpaceRow implements ITopRow {
    @Override
    public Type getType() {
        return Type.SPACE;
    }
}
