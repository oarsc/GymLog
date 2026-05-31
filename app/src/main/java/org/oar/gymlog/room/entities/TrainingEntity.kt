package org.oar.gymlog.room.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import org.oar.gymlog.util.Constants
import java.time.LocalDateTime

@Entity(
    tableName = "training",
    foreignKeys = [
        ForeignKey(
            entity = GymEntity::class,
            parentColumns = ["gymId"],
            childColumns = ["gymId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE),
        ForeignKey(
            entity = WorkoutEntity::class,
            parentColumns = ["workoutId"],
            childColumns = ["workoutId"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.CASCADE),
    ],
    indices = [
        Index("start", "end"),
        Index("gymId"),
        Index("trainingId"),
        Index("workoutId"),
    ]
)
class TrainingEntity {
    @PrimaryKey(autoGenerate = true)
    var trainingId = 0
    var workoutId: Int? = null
    var gymId = 0
    var start: LocalDateTime = Constants.DATE_ZERO
    var end: LocalDateTime? = null
    var note: String = ""
}