package org.oar.gymlog.room.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import org.oar.gymlog.room.entities.WorkoutSetEntity

@Dao
interface WorkoutSetDao {
    @Query("SELECT * FROM workout_set")
    fun getAll(): List<WorkoutSetEntity>

    @Update
    fun update(vararg variations: WorkoutSetEntity)

    @Insert
    fun insert(variation: WorkoutSetEntity): Long
}
