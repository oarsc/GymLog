package org.scp.gymlog.room.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import org.scp.gymlog.model.ExerciseType
import org.scp.gymlog.model.GymRelation
import org.scp.gymlog.model.WeightSpecification

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
        ForeignKey(
            entity = GymEntity::class,
            parentColumns = ["gymId"],
            childColumns = ["gymId"],
            onDelete = ForeignKey.CASCADE,
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
    var gymId: Int? = null
    var gymRelation = GymRelation.NO_RELATION
    var def = false

    // Last configs
    var lastWeightSpec = WeightSpecification.TOTAL_WEIGHT
    var lastStep = 500
    var lastRestTime = -1
    var lastBarId: Int? = null
}