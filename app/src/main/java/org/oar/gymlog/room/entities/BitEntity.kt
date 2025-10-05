package org.oar.gymlog.room.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import org.oar.gymlog.util.Constants
import org.oar.gymlog.util.JsonUtils.JsonFieldName
import org.oar.gymlog.util.JsonUtils.NoJsonify
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
    @JsonFieldName("v")
    var variationId = 0
    @JsonFieldName("t")
    var trainingId = 0
    var reps = 0
    @JsonFieldName("w")
    var totalWeight = 0
    var kilos = false
    var instant = false
    var note: String = ""
    @JsonFieldName("s")
    var timestamp: LocalDateTime = Constants.DATE_ZERO
    var superSet = 0
}