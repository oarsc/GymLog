package org.scp.gymlog.room.entities

import androidx.room.*
import org.scp.gymlog.model.ExerciseType
import org.scp.gymlog.model.WeightSpecification
import org.scp.gymlog.util.Constants
import org.scp.gymlog.util.JsonUtils.NoJsonify

@Entity(
    tableName = "exercise",
    foreignKeys = [
        ForeignKey(
            entity = BarEntity::class,
            parentColumns = ["barId"],
            childColumns = ["lastBarId"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.CASCADE),
    ],
    indices = [
        Index("lastBarId"),
        Index("exerciseId", "lastTrained"),
    ]
)
class ExerciseEntity {
    @PrimaryKey(autoGenerate = true)
    var exerciseId = 0
    var name = ""
    var image = ""
    var type = ExerciseType.BARBELL

    // Last configs
    @NoJsonify
    var lastTrained = Constants.DATE_ZERO
    var lastWeightSpec = WeightSpecification.NO_BAR_WEIGHT
    var lastStep = 500
    var lastRestTime = -1
    var lastBarId: Int? = null

    class WithMusclesAndVariations {
        @Embedded
        var exercise: ExerciseEntity? = null

        @Relation(
            parentColumn = "exerciseId",
            entityColumn = "muscleId",
            associateBy = Junction(ExerciseMuscleCrossRef::class))
        var primaryMuscles: List<MuscleEntity>? = null

        @Relation(
            parentColumn = "exerciseId",
            entityColumn = "muscleId",
            associateBy = Junction(SecondaryExerciseMuscleCrossRef::class))
        var secondaryMuscles: List<MuscleEntity>? = null

        @Relation(
            parentColumn = "exerciseId",
            entityColumn = "variationId",
            associateBy = Junction(VariationEntity::class))
        var variations: List<VariationEntity>? = null
    }
}