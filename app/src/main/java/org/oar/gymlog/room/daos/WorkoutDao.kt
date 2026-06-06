package org.oar.gymlog.room.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import org.oar.gymlog.room.entities.WorkoutEntity

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workout")
    fun getAll(): List<WorkoutEntity>

    @Update
    fun update(vararg variations: WorkoutEntity)

    @Insert
    fun insert(variation: WorkoutEntity): Long
}
