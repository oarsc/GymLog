package org.oar.gymlog.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workout_set",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutExerciseEntity::class,
            parentColumns = ["workoutExerciseId"],
            childColumns = ["workoutExerciseId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE),
    ],
    indices = [
        Index("workoutExerciseId", "set_order"),
    ]
)
class WorkoutSetEntity {
    @PrimaryKey(autoGenerate = true)
    var workoutSetId = 0
    var workoutExerciseId = 0
    @ColumnInfo(name = "set_order")
    var order = 0
    var totalWeight = 0
    var kilos = true
    var reps = 0
    var note = ""
    var restTime = -1
    var warmUp = false
}