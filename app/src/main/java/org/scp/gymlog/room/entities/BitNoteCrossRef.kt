package org.scp.gymlog.room.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "bit_x_note",
    primaryKeys = ["bitId", "noteId"],
    foreignKeys = [
        ForeignKey(
            entity = BitEntity::class,
            parentColumns = ["bitId"],
            childColumns = ["bitId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE),
        ForeignKey(
            entity = NoteEntity::class,
            parentColumns = ["noteId"],
            childColumns = ["noteId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE),
    ],
    indices = [
        Index("bitId"),
        Index("noteId"),
    ]
)
class BitNoteCrossRef {
    var bitId = 0
    var noteId = 0
}