package org.oar.gymlog.room.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(
    tableName = "workout_exercise",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutEntity::class,
            parentColumns = ["workoutId"],
            childColumns = ["workoutId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE),
        ForeignKey(
            entity = VariationEntity::class,
            parentColumns = ["variationId"],
            childColumns = ["variationId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE),
    ],
    indices = [
        Index("workoutId"),
        Index("variationId")
    ]
)
class WorkoutExerciseEntity {
    @PrimaryKey(autoGenerate = true)
    var workoutExerciseId = 0
    var workoutId = 0
    var variationId = 0
    var superSet = 0

    class WithWorkoutSets {
        @Embedded
        var workoutExercise: WorkoutExerciseEntity? = null

        @Relation(
            parentColumn = "workoutExerciseId",
            entityColumn = "workoutExerciseId")
        var sets: List<WorkoutSetEntity> = emptyList()
    }
}