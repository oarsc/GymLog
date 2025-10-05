package org.oar.gymlog.room.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import org.oar.gymlog.room.entities.MuscleEntity
import org.oar.gymlog.room.entities.MuscleEntity.WithExercises

@Dao
interface MuscleDao {
    @Query("SELECT * FROM muscle")
    fun getOnlyMuscles(): List<MuscleEntity>

    @Transaction
    @Query("SELECT * FROM muscle")
    fun getAll(): List<WithExercises>

    @Transaction
    @Query("SELECT * FROM muscle WHERE muscleId IN (:muscleIds)")
    fun getByIds(vararg muscleIds: Int): List<WithExercises>

    @Insert
    fun insertAll(muscles: List<MuscleEntity>)
}
