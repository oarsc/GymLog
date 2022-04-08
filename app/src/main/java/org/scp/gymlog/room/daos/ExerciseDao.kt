package org.scp.gymlog.room.daos

import androidx.room.*
import org.scp.gymlog.room.entities.ExerciseEntity
import org.scp.gymlog.room.entities.ExerciseEntity.WithMusclesAndVariations
import java.util.*

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercise")
    fun getAll(): List<ExerciseEntity>

    @Query("SELECT * FROM exercise WHERE exerciseId = :exerciseId")
    fun getById(exerciseId: Int): Optional<ExerciseEntity>

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
