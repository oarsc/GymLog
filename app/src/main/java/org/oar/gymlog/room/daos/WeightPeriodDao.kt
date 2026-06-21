package org.oar.gymlog.room.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import org.oar.gymlog.room.entities.WeightPeriodEntity
import org.oar.gymlog.room.entities.WeightPeriodEntity.WithModifications
import org.oar.gymlog.room.entities.WeightPeriodModificationEntity
import java.time.LocalDate

@Dao
interface WeightPeriodDao {
    @Query("SELECT * FROM weight_period")
    fun getAll(): List<WeightPeriodEntity>

    @Query("SELECT * FROM weight_period WHERE start <= :date AND :date < `end`")
    fun getWeightPeriodByDate(date: LocalDate): WeightPeriodEntity?

    @Query("SELECT * FROM weight_period WHERE weightPeriodId = :weightPeriodId")
    fun getWeightPeriodWithModifications(weightPeriodId: Int): List<WithModifications>

    @Query("SELECT * FROM weight_period_modification WHERE weightPeriodId = :weightPeriodId")
    fun getModificationsByPeriodId(weightPeriodId: Int): List<WeightPeriodModificationEntity>

    @Update
    fun update(weightPeriodEntity: WeightPeriodEntity)

    @Update
    fun updateModification(weightPeriodModificationEntity: WeightPeriodModificationEntity)

    @Insert
    fun insert(weightPeriodEntity: WeightPeriodEntity): Long

    @Insert
    fun insertAll(weightPeriodEntities: List<WeightPeriodEntity>): LongArray

    @Insert
    fun insertModification(weightPeriodModificationEntity: WeightPeriodModificationEntity): Long

    @Insert
    fun insertAllModifications(weightPeriodModificationEntities: List<WeightPeriodModificationEntity>): LongArray

    @Delete
    fun delete(weightPeriodEntity: WeightPeriodEntity)

    @Delete
    fun deleteModification(weightPeriodModificationEntity: WeightPeriodModificationEntity)
}
