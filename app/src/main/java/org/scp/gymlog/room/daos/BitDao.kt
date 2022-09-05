package org.scp.gymlog.room.daos

import androidx.room.*
import org.scp.gymlog.room.entities.BitEntity
import java.time.LocalDateTime
import java.util.*

@Dao
interface BitDao {
    @Query("SELECT * FROM bit")
    fun getAll(): List<BitEntity>

    @Query("SELECT * FROM bit WHERE exerciseId = :exerciseId AND variationId IS NULL " +
                "ORDER BY trainingId DESC, timestamp LIMIT :limit")
    fun getHistory(exerciseId: Int, limit: Int): List<BitEntity>

    @Query("SELECT * FROM bit WHERE exerciseId = :exerciseId AND variationId = :variationId " +
                "ORDER BY trainingId DESC, timestamp LIMIT :limit")
    fun getHistory(exerciseId: Int, variationId: Int, limit: Int): List<BitEntity>

    @Query("SELECT * FROM bit WHERE exerciseId = :exerciseId AND variationId IS NULL AND " +
                "(trainingId = :trainingId AND timestamp > :date OR trainingId < :trainingId) " +
                "ORDER BY trainingId DESC, timestamp LIMIT :limit")
    fun getHistory(exerciseId: Int, trainingId: Int, date: LocalDateTime, limit: Int): List<BitEntity>

    @Query("SELECT * FROM bit WHERE exerciseId = :exerciseId AND variationId = :variationId AND " +
                "(trainingId = :trainingId AND timestamp > :date OR trainingId < :trainingId) " +
                "ORDER BY trainingId DESC, timestamp LIMIT :limit")
    fun getHistory(exerciseId: Int, variationId: Int, trainingId: Int, date: LocalDateTime, limit: Int): List<BitEntity>

    @Query("SELECT * FROM bit WHERE :dateStart <= timestamp AND timestamp < :dateEnd ORDER BY timestamp")
    fun getHistory(dateStart: LocalDateTime, dateEnd: LocalDateTime): List<BitEntity>

    @Query("SELECT * FROM bit WHERE trainingId = :trainingId ORDER BY timestamp")
    fun getHistoryByTrainingId(trainingId: Int): List<BitEntity>

    @Query("SELECT timestamp FROM bit WHERE trainingId = :trainingId ORDER BY timestamp DESC LIMIT 1")
    fun getMostRecentTimestampByTrainingId(trainingId: Int): LocalDateTime?

    @Query("SELECT timestamp FROM bit WHERE trainingId = :trainingId ORDER BY timestamp ASC LIMIT 1")
    fun getFirstTimestampByTrainingId(trainingId: Int): LocalDateTime?

    @Query("SELECT DISTINCT note FROM bit WHERE exerciseId = :exerciseId AND note <> '' " +
                "ORDER BY timestamp DESC LIMIT :limit")
    fun getNotesHistory(exerciseId: Int, limit: Int): List<String>

    @Query("SELECT MAX(reps) AS reps, * FROM bit " +
                "WHERE exerciseId = :exerciseId GROUP BY totalWeight, variationId ORDER BY timestamp")
    fun findTops(exerciseId: Int): List<BitEntity>

    @Query("SELECT * FROM bit WHERE exerciseId = :exerciseId AND variationId IS NULL AND totalWeight = :weight ORDER BY timestamp DESC")
    fun findAllByExerciseAndWeight(exerciseId: Int, weight: Int): List<BitEntity>

    @Query("SELECT * FROM bit WHERE exerciseId = :exerciseId AND variationId = :variationId AND totalWeight = :weight ORDER BY timestamp DESC")
    fun findAllByExerciseAndWeight(exerciseId: Int, variationId: Int, weight: Int ): List<BitEntity>

    @Insert
    fun insert(bit: BitEntity): Long

    @Insert
    fun insertAll(bit: List<BitEntity>): LongArray

    @Delete
    fun delete(bit: BitEntity)

    @Update
    fun update(bit: BitEntity)

    @Query("DELETE FROM bit")
    fun clear()
}