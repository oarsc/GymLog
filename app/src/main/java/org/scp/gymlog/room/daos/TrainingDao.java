package org.scp.gymlog.room.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import org.scp.gymlog.room.entities.TrainingEntity;

import java.util.List;
import java.util.Optional;

@Dao
public interface TrainingDao {
    @Query("SELECT * FROM training")
    List<TrainingEntity> getAll();

    @Query("SELECT * FROM training WHERE `end` IS NULL")
    Optional<TrainingEntity> getCurrentTraining();

    @Insert
    long[] insertAll(TrainingEntity... training);

    @Insert
    long insert(TrainingEntity training);

    @Delete
    void delete(TrainingEntity training);
}
