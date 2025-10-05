package org.oar.gymlog.room.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import org.oar.gymlog.room.entities.ExerciseMuscleCrossRef
import org.oar.gymlog.room.entities.SecondaryExerciseMuscleCrossRef

@Dao
interface ExerciseMuscleCrossRefDao {
    @Query("SELECT * FROM exercise_x_muscle_group")
    fun getAll(): List<ExerciseMuscleCrossRef>

    @Insert
    fun insert(exercise: ExerciseMuscleCrossRef): Long

    @Insert
    fun insertAll(exercises: List<ExerciseMuscleCrossRef>): LongArray

    @Delete
    fun delete(exercise: ExerciseMuscleCrossRef)

    @Query("DELETE FROM exercise_x_muscle_group WHERE exerciseId = :exerciseId")
    fun clearMusclesFromExercise(exerciseId: Int): Int

    @Query("DELETE FROM exercise_x_muscle_group")
    fun clear()



    @Query("SELECT * FROM secondary_exercise_x_muscle_group")
    fun getAllSecondaryMuscles(): List<SecondaryExerciseMuscleCrossRef>

    @Insert
    fun insert(exercise: SecondaryExerciseMuscleCrossRef): Long

    @Insert
    fun insertAllSecondaries(exercises: List<SecondaryExerciseMuscleCrossRef>): LongArray

    @Delete
    fun delete(exercise: SecondaryExerciseMuscleCrossRef)

    @Query("DELETE FROM secondary_exercise_x_muscle_group WHERE exerciseId = :exerciseId")
    fun clearSecondaryMusclesFromExercise(exerciseId: Int): Int

    @Query("DELETE FROM secondary_exercise_x_muscle_group")
    fun clearSecondary()
}
