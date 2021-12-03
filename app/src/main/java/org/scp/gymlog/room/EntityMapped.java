package org.scp.gymlog.room;

public interface EntityMapped<T> {
    T toEntity();
    <E extends EntityMapped> E fromEntity(T entity);
}
