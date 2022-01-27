package org.scp.gymlog.room.daos;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import org.scp.gymlog.room.entities.VariationEntity;

import java.util.List;
import java.util.Optional;

@Dao
public interface VariationDao {
    @Query("SELECT * FROM variation")
    List<VariationEntity> getAll();

    @Query("SELECT * FROM variation WHERE variationId = :variationId")
    Optional<VariationEntity> getVariation(int variationId);

    @Query("SELECT * FROM variation WHERE exerciseId = :exerciseId")
    List<VariationEntity> getVariationByExerciseId(int exerciseId);

    @Update
    void updateAll(VariationEntity... variation);

    @Insert
    long insert(VariationEntity variation);

    @Insert
    long[] insertAll(VariationEntity... variation);
}
