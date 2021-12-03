package org.scp.gymlog.room;

public interface EntityMapped<T> {
    T toEntity();
    void fromEntity(T entity);
}
