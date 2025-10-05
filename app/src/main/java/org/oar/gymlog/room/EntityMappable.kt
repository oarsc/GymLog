package org.oar.gymlog.room

interface EntityMappable<T> {
    fun toEntity(): T
}
