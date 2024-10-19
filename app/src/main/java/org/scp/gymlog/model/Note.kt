package org.scp.gymlog.model

import org.scp.gymlog.exceptions.SaveException
import org.scp.gymlog.room.EntityMappable

import org.scp.gymlog.room.entities.NoteEntity
import org.scp.gymlog.util.Data

data class Note (
    var id: Int = 0,
    var content: String
): EntityMappable<NoteEntity> {

    constructor(entity: NoteEntity)
        : this(entity.noteId, entity.content)

    override fun toEntity(): NoteEntity {
        if (content.isBlank()) {
            throw SaveException("Can't convert note to entity with empty content")
        }
        return NoteEntity().apply {
            noteId = id.coerceAtLeast(0)
            content = this@Note.content
        }
    }

    companion object {
        fun String.toNotes(): List<Note> =
            this.split(",")
                .map(String::trim)
                .filter(String::isNotEmpty)
                .map { Data.getNoteOrCreate(it) }
    }
}