package org.scp.gymlog.room.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import org.scp.gymlog.room.entities.NoteEntity

@Dao
interface NoteDao {
    // NOTES HISTORY
    @Query("SELECT DISTINCT content FROM note " +
        "JOIN bit_x_note ON note.noteId = bit_x_note.noteId " +
        "JOIN bit ON bit_x_note.bitId = bit.bitId " +
        "WHERE variationId = :variationId ORDER BY timestamp DESC LIMIT :limit")
    fun getNotesHistory(variationId: Int, limit: Int): List<String>

    @Query("SELECT DISTINCT content FROM note " +
        "JOIN bit_x_note ON note.noteId = bit_x_note.noteId " +
        "JOIN bit ON bit_x_note.bitId = bit.bitId " +
        "JOIN training ON bit.trainingId = training.trainingId " +
        "WHERE gymId = :gymId AND variationId = :variationId " +
        "ORDER BY timestamp DESC LIMIT :limit")
    fun getNotesHistory(gymId: Int, variationId: Int, limit: Int): List<String>

    @Query("SELECT * FROM note")
    fun getAll(): List<NoteEntity>

    @Insert
    fun insert(note: NoteEntity): Long

    @Insert
    fun insertAll(notes: List<NoteEntity>): LongArray

    @Delete
    fun delete(bit: NoteEntity)

    @Update
    fun update(bit: NoteEntity)

    @Query("DELETE FROM note")
    fun clear()
}