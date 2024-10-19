package org.scp.gymlog.room.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import org.scp.gymlog.util.JsonUtils.NoJsonify

@Entity(
    tableName = "note",
    indices = [
        Index("noteId"),
        Index("content", unique = true),
    ]
)
class NoteEntity {
    @NoJsonify
    @PrimaryKey(autoGenerate = true)
    var noteId = 0
    var content = ""
}