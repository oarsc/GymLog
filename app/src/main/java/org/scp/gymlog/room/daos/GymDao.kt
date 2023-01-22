package org.scp.gymlog.room.daos

import androidx.room.*
import org.scp.gymlog.room.entities.GymEntity

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
