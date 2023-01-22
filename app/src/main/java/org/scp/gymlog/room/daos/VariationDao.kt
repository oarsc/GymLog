package org.scp.gymlog.room.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import org.scp.gymlog.room.entities.VariationEntity

@Dao
interface VariationDao {
    @Query("SELECT * FROM variation")
    fun getAllFromAllGyms(): List<VariationEntity>

    @Update
    fun update(vararg variations: VariationEntity)

    @Update
    fun updateAll(variations: List<VariationEntity>)

    @Insert
    fun insert(variation: VariationEntity): Long

    @Insert
    fun insertAll(variation: List<VariationEntity>): LongArray
}
