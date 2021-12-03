package org.scp.gymlog.room.daos;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import org.scp.gymlog.room.entities.MuscleEntity;

import java.util.List;

@Dao
public interface MuscleDao {
    @Query("SELECT * FROM muscle")
    List<MuscleEntity> getOnlyMuscles();

    @Transaction
    @Query("SELECT * FROM muscle")
    List<MuscleEntity.WithExercises> getAll();

    @Transaction
    @Query("SELECT * FROM muscle WHERE muscleId IN (:muscleIds)")
    List<MuscleEntity.WithExercises> getByIds(int... muscleIds);

    @Insert
    void insertAll(MuscleEntity... muscles);
}
