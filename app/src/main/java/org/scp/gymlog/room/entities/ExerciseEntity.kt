package org.scp.gymlog.room.entities

import androidx.room.*
import org.scp.gymlog.util.Constants
import org.scp.gymlog.util.JsonUtils.NoJsonify

@Entity(
    tableName = "exercise",
    indices = [
        Index("exerciseId", "lastTrained"),
    ]
)
class ExerciseEntity {
    @PrimaryKey(autoGenerate = true)
    var exerciseId = 0
    var name = ""
    var image = ""

    // Last configs
    @NoJsonify
    var lastTrained = Constants.DATE_ZERO

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