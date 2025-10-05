package org.oar.gymlog.room.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import org.oar.gymlog.room.entities.GymEntity

@Dao
interface GymDao {
    @Query("SELECT * FROM gym ORDER BY gymId")
    fun getAll(): List<GymEntity>

    @Insert
    fun insertAll(gyms: List<GymEntity>): LongArray

    @Insert
    fun insert(gym: GymEntity): Long

    @Delete
    fun delete(gym: GymEntity)

    @Update
    fun update(gym: GymEntity)

    @Query("DELETE FROM gym")
    fun clear()
}
