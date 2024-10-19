package org.scp.gymlog.room.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import org.scp.gymlog.room.entities.BitEntity
import org.scp.gymlog.room.entities.BitEntity.BitEntityWithNotes
import java.time.LocalDateTime

@Dao
interface BitDao {
    @Query("SELECT * FROM bit")
    fun getAllFromAllGyms(): List<BitEntityWithNotes>

    // GET REGISTRY FIRST PAGE
    @Query("SELECT * FROM bit WHERE " +
            "variationId = :variationId ORDER BY trainingId DESC, timestamp LIMIT :limit")
    fun getHistory(variationId: Int, limit: Int): List<BitEntityWithNotes>

    @Query("SELECT bit.* FROM bit " +
        "JOIN training ON bit.trainingId = training.trainingId " +
        "WHERE gymId = :gymId AND variationId = :variationId " +
        "ORDER BY trainingId DESC, timestamp LIMIT :limit")
    fun getHistory(gymId: Int, variationId: Int, limit: Int): List<BitEntityWithNotes>

    // GET REGISTRY NEXT PAGES
    @Query("SELECT * FROM bit WHERE " +
        "variationId = :variationId AND " +
        "(trainingId = :trainingId AND timestamp > :date OR trainingId < :trainingId) " +
        "ORDER BY trainingId DESC, timestamp LIMIT :limit")
    fun getHistory(variationId: Int, trainingId: Int, date: LocalDateTime, limit: Int): List<BitEntityWithNotes>

    @Query("SELECT bit.* FROM bit " +
        "JOIN training ON bit.trainingId = training.trainingId WHERE " +
        "gymId = :gymId AND variationId = :variationId AND " +
        "(bit.trainingId = :trainingId AND timestamp > :date OR bit.trainingId < :trainingId) " +
        "ORDER BY bit.trainingId DESC, timestamp LIMIT :limit")
    fun getHistory(gymId: Int, variationId: Int, trainingId: Int, date: LocalDateTime, limit: Int): List<BitEntityWithNotes>

    // CALENDAR REGISTRY
    @Query("SELECT * FROM bit WHERE :dateStart <= timestamp AND timestamp < :dateEnd ORDER BY timestamp")
    fun getCalendarHistory(dateStart: LocalDateTime, dateEnd: LocalDateTime): List<BitEntity>

    @Query("SELECT * FROM bit WHERE trainingId = :trainingId ORDER BY timestamp")
    fun getHistoryByTrainingId(trainingId: Int): List<BitEntityWithNotes>

    @Query("SELECT * FROM bit WHERE trainingId = :trainingId ORDER BY timestamp DESC")
    fun getHistoryByTrainingIdDesc(trainingId: Int): List<BitEntityWithNotes>

    // MOST/LEAST RECENT
    @Query("SELECT * FROM bit WHERE trainingId == :trainingId AND timestamp < :timestamp ORDER BY timestamp DESC LIMIT 1")
    fun getPreviousByTraining(trainingId: Int, timestamp: LocalDateTime): BitEntity?

    @Query("SELECT * FROM bit WHERE trainingId == :trainingId AND timestamp > :timestamp ORDER BY timestamp ASC LIMIT 1")
    fun getNextByTraining(trainingId: Int, timestamp: LocalDateTime): BitEntity?

    @Query("SELECT * FROM bit WHERE trainingId = :trainingId ORDER BY timestamp DESC LIMIT 1")
    fun getMostRecentByTrainingId(trainingId: Int): BitEntity?

    @Query("SELECT timestamp FROM bit WHERE trainingId = :trainingId ORDER BY timestamp ASC LIMIT 1")
    fun getFirstTimestampByTrainingId(trainingId: Int): LocalDateTime?

    @Query("SELECT MAX(superSet) FROM bit WHERE trainingId = :trainingId")
    fun getMaxSuperSet(trainingId: Int): Int?

    // TOPS PAGES
    @Query("SELECT MAX(reps) AS reps, bit.* FROM bit " +
        "JOIN training ON bit.trainingId = training.trainingId " +
        "JOIN variation ON bit.variationId = variation.variationId " +
        "WHERE (training.gymId = :gymId OR gymRelation == 0) AND exerciseId = :exerciseId " +
        "GROUP BY totalWeight, bit.variationId ORDER BY timestamp")
    fun findTops(gymId: Int, exerciseId: Int): List<BitEntity>

    @Query("SELECT * FROM bit WHERE " +
            "variationId = :variationId AND totalWeight = :weight ORDER BY timestamp DESC")
    fun findAllByExerciseAndWeight(variationId: Int, weight: Int): List<BitEntity>

    @Query("SELECT bit.* FROM bit " +
        "JOIN training ON bit.trainingId = training.trainingId " +
        "WHERE gymId = :gymId AND variationId = :variationId AND totalWeight = :weight " +
        "ORDER BY timestamp DESC")
    fun findAllByExerciseAndWeight(gymId: Int, variationId: Int, weight: Int): List<BitEntity>

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