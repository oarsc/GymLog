package org.scp.gymlog.room.daos

import androidx.room.*
import org.scp.gymlog.room.entities.BarEntity

@Dao
interface BarDao {
    @Query("SELECT * FROM bar")
    fun getAll(): List<BarEntity>

    @Insert
    fun insertAll(bar: List<BarEntity>): LongArray

    @Insert
    fun insert(bar: BarEntity): Long

    @Delete
    fun delete(bar: BarEntity)

    @Update
    fun update(bar: BarEntity)

    @Query("DELETE FROM bar")
    fun clear()
}
