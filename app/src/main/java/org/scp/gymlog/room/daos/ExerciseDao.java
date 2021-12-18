package org.scp.gymlog.room.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import org.scp.gymlog.room.entities.ExerciseEntity;

import java.util.List;

@Dao
public interface ExerciseDao {
    @Query("SELECT * FROM exercise")
    List<ExerciseEntity> getOnlyExercises();

    @Transaction
    @Query("SELECT * FROM exercise")
    List<ExerciseEntity.WithMuscles> getAll();

    @Query("SELECT exerciseId FROM exercise WHERE exerciseId IN "+
            "(SELECT exerciseId FROM exercise_x_muscle_group WHERE muscleId = :muscleId) "+
            "ORDER BY lastTrained DESC, name")
    List<Integer> getOrderedExercises(int muscleId);

    @Transaction
    @Query("SELECT * FROM exercise WHERE exerciseId IN (:exerciseIds)")
    List<ExerciseEntity.WithMuscles> getByIds(int... exerciseIds);

    @Insert
    long insert(ExerciseEntity exercise);

    @Insert
    long[] insertAll(ExerciseEntity... exercises);

    @Delete
    void delete(ExerciseEntity... exercises);

    @Update
    void update(ExerciseEntity... exercises);
}
