package org.scp.gymlog.ui.top.rows;

public interface ITopRow {
    enum Type {
        VARIATION, HEADER, BIT, SPACE
    }
    Type getType();
}
