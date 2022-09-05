package org.scp.gymlog.room.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import org.scp.gymlog.model.ExerciseType
import org.scp.gymlog.model.WeightSpecification
import org.scp.gymlog.util.Constants
import org.scp.gymlog.util.JsonUtils

@Entity(
    tableName = "variation",
    foreignKeys = [
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["exerciseId"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE),
        ForeignKey(
            entity = BarEntity::class,
            parentColumns = ["barId"],
            childColumns = ["lastBarId"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.CASCADE),
    ],
    indices = [
        Index("lastBarId"),
        Index("variationId"),
        Index("exerciseId"),
    ]
)
class VariationEntity {
    @PrimaryKey(autoGenerate = true)
    var variationId = 0
    var name = ""
    var exerciseId = 0
    var type = ExerciseType.BARBELL
    var def = false

    // Last configs
    var lastWeightSpec = WeightSpecification.NO_BAR_WEIGHT
    var lastStep = 500
    var lastRestTime = -1
    var lastBarId: Int? = null
}