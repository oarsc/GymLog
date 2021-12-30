package org.scp.gymlog.room.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import org.scp.gymlog.room.entities.TrainingEntity;

import java.util.List;
import java.util.Optional;

@Dao
public interface TrainingDao {
    @Query("SELECT * FROM training")
    List<TrainingEntity> getAll();

    @Query("SELECT * FROM training WHERE `end` IS NULL")
    Optional<TrainingEntity> getCurrentTraining();

    @Query("SELECT * FROM training WHERE trainingId = :trainingId")
    Optional<TrainingEntity> getTraining(int trainingId);

    @Update
    void update(TrainingEntity training);

    @Insert
    long insert(TrainingEntity training);

    @Delete
    void delete(TrainingEntity training);

    @Query("DELETE FROM training WHERE trainingId NOT IN (SELECT DISTINCT trainingId FROM bit)")
    int deleteEmptyTraining();
}
