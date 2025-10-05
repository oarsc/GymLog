package org.oar.gymlog.room.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import org.oar.gymlog.room.entities.BarEntity

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
