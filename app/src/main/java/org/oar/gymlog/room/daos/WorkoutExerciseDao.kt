package org.oar.gymlog.room.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import org.oar.gymlog.room.entities.WorkoutExerciseEntity

@Dao
interface WorkoutExerciseDao {
    @Query("SELECT * FROM workout_exercise")
    fun getAll(): List<WorkoutExerciseEntity>

    @Update
    fun update(vararg variations: WorkoutExerciseEntity)

    @Insert
    fun insert(variation: WorkoutExerciseEntity): Long
}
