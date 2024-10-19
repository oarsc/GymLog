package org.scp.gymlog.room.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import org.scp.gymlog.room.entities.BitNoteCrossRef

@Dao
interface BitNoteCrossRefDao {
    @Insert
    fun insert(bitNote: BitNoteCrossRef): Long

    @Insert
    fun insertAll(bitNotes: List<BitNoteCrossRef>): LongArray

    @Delete
    fun delete(bitNote: BitNoteCrossRef)

    @Query("DELETE FROM bit_x_note WHERE bitId = :bitId")
    fun clearForBit(bitId: Int)

    @Query("DELETE FROM bit_x_note")
    fun clear()
}
