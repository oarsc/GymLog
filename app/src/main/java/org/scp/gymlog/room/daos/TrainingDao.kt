package org.scp.gymlog.room.daos

import androidx.room.*
import org.scp.gymlog.room.entities.TrainingEntity
import java.time.LocalDateTime
import java.util.*

@Dao
interface TrainingDao {
    @Query("SELECT * FROM training")
    fun getAll(): List<TrainingEntity>

    @Query("SELECT MAX(trainingId) FROM training")
    fun getMaxTrainingId(): Int?

    @Query("SELECT * FROM training WHERE `end` IS NULL")
    fun getCurrentTraining(): TrainingEntity?

    @Query("SELECT * FROM training WHERE trainingId = :trainingId")
    fun getTraining(trainingId: Int): TrainingEntity?

    @Query("SELECT * FROM training WHERE :startDate <= start AND start < :endDate")
    fun getTrainingByStartDate(startDate: LocalDateTime, endDate: LocalDateTime): List<TrainingEntity>

    @Query("SELECT MIN(start) FROM training")
    fun getFirstTrainingStartDate(): LocalDateTime?

    @Update
    fun update(training: TrainingEntity)

    @Query("UPDATE training SET trainingId = :newId WHERE trainingId = :oldId")
    fun updateId(oldId: Int, newId: Int): Int

    @Insert
    fun insert(training: TrainingEntity): Long

    @Insert
    fun insertAll(training: List<TrainingEntity>): LongArray

    @Delete
    fun delete(training: TrainingEntity)

    @Query("DELETE FROM training WHERE trainingId NOT IN (SELECT DISTINCT trainingId FROM bit)")
    fun deleteEmptyTraining(): Int

    @Query("DELETE FROM training WHERE trainingId <> :trainingId AND trainingId NOT IN (SELECT DISTINCT trainingId FROM bit)")
    fun deleteEmptyTrainingExcept(trainingId: Int): Int

    @Query("DELETE FROM training")
    fun clear()
}
