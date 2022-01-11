package org.scp.gymlog.room.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import org.scp.gymlog.room.entities.ExerciseEntity;

import java.util.List;
import java.util.Optional;

@Dao
public interface ExerciseDao {
    @Query("SELECT * FROM exercise")
    List<ExerciseEntity> getAll();

    @Query("SELECT * FROM exercise WHERE exerciseId = :exerciseId")
    Optional<ExerciseEntity> getById(int exerciseId);

    @Transaction
    @Query("SELECT * FROM exercise")
    List<ExerciseEntity.WithMuscles> getAllWithMuscles();

    @Query("SELECT exerciseId FROM exercise_x_muscle_group WHERE muscleId = :muscleId")
    List<Integer> getExercisesIdByMuscleId(int muscleId);

    @Transaction
    @Query("SELECT * FROM exercise WHERE exerciseId IN (:exerciseIds)")
    List<ExerciseEntity.WithMuscles> getByIds(int... exerciseIds);

    @Insert
    long insert(ExerciseEntity exercise);

    @Insert
    long[] insertAll(ExerciseEntity... exercises);

    @Delete
    int delete(ExerciseEntity... exercises);

    @Update
    void update(ExerciseEntity... exercises);

    @Query("DELETE FROM exercise")
    void clear();
}
