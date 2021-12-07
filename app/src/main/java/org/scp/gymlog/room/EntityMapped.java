package org.scp.gymlog.room;

import androidx.annotation.NonNull;

public interface EntityMapped<T> {
    T toEntity();
    <E extends EntityMapped> E fromEntity(@NonNull T entity);
}
