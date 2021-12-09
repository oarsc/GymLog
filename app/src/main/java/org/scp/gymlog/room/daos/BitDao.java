package org.scp.gymlog.room.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import org.scp.gymlog.room.entities.BitEntity;

import java.util.Date;
import java.util.List;

@Dao
public interface BitDao {
    @Query("SELECT * FROM bit WHERE exerciseId = :exerciseId AND trainingId = :trainingId ORDER BY timestamp")
    List<BitEntity> getHistory(int exerciseId, int trainingId);

    @Query("SELECT * FROM bit WHERE exerciseId = :exerciseId AND timestamp > :date " +
            "ORDER BY timestamp DESC LIMIT :limit")
    List<BitEntity> getHistory(int exerciseId, Date date, int limit);

    @Insert
    long insert(BitEntity bit);

    @Delete
    void delete(BitEntity bit);

    @Update
    void update(BitEntity bit);
}
