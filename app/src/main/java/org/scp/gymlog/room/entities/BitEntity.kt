package org.scp.gymlog.room.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import org.scp.gymlog.util.Constants
import org.scp.gymlog.util.JsonUtils.NoJsonify
import java.time.LocalDateTime

@Entity(
    tableName = "bit",
    foreignKeys = [
        ForeignKey(
            entity = VariationEntity::class,
            parentColumns = ["variationId"],
            childColumns = ["variationId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE),
        ForeignKey(
            entity = TrainingEntity::class,
            parentColumns = ["trainingId"],
            childColumns = ["trainingId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE),
    ],
    indices = [
        Index("trainingId"),
        Index("trainingId", "timestamp"),
        Index("variationId"),
        Index("variationId", "timestamp"),
        Index("variationId", "trainingId", "timestamp"),
    ]
)
class BitEntity {
    @NoJsonify
    @PrimaryKey(autoGenerate = true)
    var bitId = 0
    var variationId = 0
    var trainingId = 0
    var reps = 0
    var totalWeight = 0
    var kilos = false
    var instant = false
    var note: String = ""
    var timestamp: LocalDateTime = Constants.DATE_ZERO
    var superSet = 0
}