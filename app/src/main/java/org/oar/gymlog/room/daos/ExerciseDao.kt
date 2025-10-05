package org.oar.gymlog.room.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import org.oar.gymlog.room.entities.ExerciseEntity
import org.oar.gymlog.room.entities.ExerciseEntity.WithMusclesAndVariations

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercise")
    fun getAll(): List<ExerciseEntity>

    @Query("SELECT * FROM exercise WHERE exerciseId = :exerciseId")
    fun getById(exerciseId: Int): ExerciseEntity?

    @Query("SELECT * FROM exercise")
    @Transaction
    fun getAllWithMusclesAndVariations(): List<WithMusclesAndVariations>

    @Query("SELECT exerciseId FROM exercise_x_muscle_group WHERE muscleId = :muscleId")
    fun getExercisesIdByMuscleId(muscleId: Int): List<Int>

    @Insert
    fun insert(exercise: ExerciseEntity): Long

    @Insert
    fun insertAll(vararg exercises: ExerciseEntity): LongArray

    @Delete
    fun delete(vararg exercises: ExerciseEntity): Int

    @Update
    fun update(vararg exercises: ExerciseEntity)

    @Query("DELETE FROM exercise")
    fun clear()
}
