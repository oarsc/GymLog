package org.oar.gymlog.room.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import org.oar.gymlog.room.entities.WeightEntity
import org.oar.gymlog.room.entities.WeightPeriodEntity
import org.oar.gymlog.room.entities.WeightPeriodModificationEntity
import java.time.LocalDate

@Dao
interface WeightDao {
    // ## WEIGHT ####

    @Query("SELECT * FROM weight")
    fun getAll(): List<WeightEntity>

    @Query("SELECT * FROM weight WHERE :startDate <= date AND date < :endDate")
    fun getWeightsByDate(startDate: LocalDate, endDate: LocalDate): List<WeightEntity>

    @Update
    fun update(weightEntity: WeightEntity)

    @Insert
    fun insert(weightEntity: WeightEntity): Long

    @Insert
    fun insertAll(weightEntities: List<WeightEntity>): LongArray

    @Delete
    fun delete(weightEntity: WeightEntity)

    @Query("DELETE FROM weight")
    fun clear()


    // ## WEIGHT PERIOD ####

    @Query("SELECT * FROM weight_period order by start")
    fun getAllPeriods(): List<WeightPeriodEntity>

    @Query("SELECT * FROM weight_period WHERE start <= :date AND :date < `end`")
    fun getPeriodByDate(date: LocalDate): WeightPeriodEntity?

    @Query("SELECT * FROM weight_period WHERE weightPeriodId = :weightPeriodId")
    fun getPeriod(weightPeriodId: Int): WeightPeriodEntity

    @Query("SELECT * FROM weight_period WHERE weightPeriodId = :weightPeriodId")
    fun getPeriodWithModifications(weightPeriodId: Int): WeightPeriodEntity.WithModifications

    @Update
    fun updatePeriod(weightPeriodEntity: WeightPeriodEntity)

    @Insert
    fun insertPeriod(weightPeriodEntity: WeightPeriodEntity): Long

    @Insert
    fun insertAllPeriods(weightPeriodEntities: List<WeightPeriodEntity>): LongArray

    @Delete
    fun deletePeriod(weightPeriodEntity: WeightPeriodEntity)


    // ## WEIGHT PERIOD MODIFICATION ####

    @Query("SELECT * FROM weight_period_modification WHERE weightPeriodModificationId = :weightPeriodModificationId")
    fun getModification(weightPeriodModificationId: Int): WeightPeriodModificationEntity

    @Query("SELECT * FROM weight_period_modification WHERE weightPeriodId = :weightPeriodId")
    fun getModificationsByPeriodId(weightPeriodId: Int): List<WeightPeriodModificationEntity>

    @Update
    fun updateModification(weightPeriodModificationEntity: WeightPeriodModificationEntity)

    @Insert
    fun insertModification(weightPeriodModificationEntity: WeightPeriodModificationEntity): Long

    @Insert
    fun insertAllModifications(weightPeriodModificationEntities: List<WeightPeriodModificationEntity>): LongArray

    @Delete
    fun deleteModification(weightPeriodModificationEntity: WeightPeriodModificationEntity)
}
