package org.scp.gymlog.room.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import org.scp.gymlog.room.entities.BarEntity;

import java.util.List;

@Dao
public interface BarDao {
    @Query("SELECT * FROM bar")
    List<BarEntity> getAll();

    @Insert
    long[] insertAll(BarEntity... bit);

    @Insert
    long insert(BarEntity bit);

    @Delete
    void delete(BarEntity bit);

    @Update
    void update(BarEntity bit);

    @Query("DELETE FROM bar")
    void clear();
}
