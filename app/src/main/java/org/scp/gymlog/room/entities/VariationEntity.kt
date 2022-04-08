package org.scp.gymlog.room.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "variation",
    foreignKeys = [
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["exerciseId"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE),
    ],
    indices = [
        Index("variationId"),
        Index("exerciseId"),
    ]
)
class VariationEntity {
    @PrimaryKey(autoGenerate = true)
    var variationId = 0
    var name = ""
    var exerciseId = 0
}