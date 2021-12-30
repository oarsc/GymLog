package org.scp.gymlog.room.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import org.scp.gymlog.room.entities.BitEntity;

import java.util.Calendar;
import java.util.List;
import java.util.Optional;

@Dao
public interface BitDao {
    @Query("SELECT * FROM bit WHERE exerciseId = :exerciseId ORDER BY trainingId DESC, timestamp LIMIT :limit")
    List<BitEntity> getHistory(int exerciseId, int limit);

    @Query("SELECT * FROM bit WHERE exerciseId = :exerciseId AND " +
            "(trainingId = :trainingId AND timestamp > :date OR trainingId < :trainingId) " +
            "ORDER BY trainingId DESC, timestamp LIMIT :limit")
    List<BitEntity> getHistory(int exerciseId, int trainingId, Calendar date, int limit);

    @Query("SELECT * FROM bit WHERE exerciseId = :exerciseId AND trainingId = :trainingId ORDER BY timestamp")
    List<BitEntity> getHistoryByTrainingId(int exerciseId, int trainingId);

    @Query("SELECT timestamp FROM bit WHERE trainingId = :trainingId ORDER BY timestamp DESC LIMIT 1")
    Optional<Calendar> getMostRecentTimestampByTrainingId(int trainingId);

    @Query("SELECT timestamp FROM bit WHERE trainingId = :trainingId ORDER BY timestamp ASC LIMIT 1")
    Optional<Calendar> getFirstTimestampByTrainingId(int trainingId);

    @Query("SELECT DISTINCT note FROM bit WHERE exerciseId = :exerciseId ORDER BY timestamp DESC "+
            "LIMIT :limit")
    List<String> getNotesHistory(int exerciseId, int limit);

    @Insert
    long insert(BitEntity bit);

    @Delete
    void delete(BitEntity bit);

    @Update
    void update(BitEntity bit);
}
