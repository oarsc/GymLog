package org.scp.gymlog.room.entities

import androidx.room.*
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
        ForeignKey(
            entity = GymEntity::class,
            parentColumns = ["gymId"],
            childColumns = ["gymId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE),
    ],
    indices = [
        Index("trainingId"),
        Index("variationId", "gymId"),
        Index("variationId", "timestamp", "gymId"),
        Index("variationId", "trainingId", "timestamp", "gymId"),
    ]
)
class BitEntity {
    @NoJsonify
    @PrimaryKey(autoGenerate = true)
    var bitId = 0
    var variationId = 0
    var trainingId = 0
    var gymId = 0
    var reps = 0
    var totalWeight = 0
    var kilos = false
    var instant = false
    var note: String = ""
    var timestamp: LocalDateTime = Constants.DATE_ZERO

    class WithBar {
        @Embedded
        var bit: BitEntity? = null

        @Relation(
            parentColumn = "barId",
            entityColumn = "barId"
        )
        var bar: BarEntity? = null
    }
}