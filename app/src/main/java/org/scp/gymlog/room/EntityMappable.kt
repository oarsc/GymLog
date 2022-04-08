package org.scp.gymlog.room

interface EntityMappable<T> {
    fun toEntity(): T
}
