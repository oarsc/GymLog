package org.scp.gymlog.room

import org.scp.gymlog.model.Bit
import org.scp.gymlog.util.Data

object DatabaseSnippets {
    fun AppDatabase.insertBitAndNotes(bit: Bit, insert: Boolean): Int {
        with(noteDao()) {
            bit.notes
                .filter { it.id <= 0 }
                .onEach { it.id = insert(it.toEntity()).toInt() }
                .also { Data.notes.addAll(it) }
        }

        if (insert) {
            bit.id = bitDao().insert(bit.toEntity()).toInt()
            bitNoteCrossRefDao().insertAll(bit.toNotesEntity())

        } else {
            bitDao().update(bit.toEntity())
            bitNoteCrossRefDao().apply {
                clearForBit(bit.id)
                insertAll(bit.toNotesEntity())
            }
        }
        return bit.id
    }
}