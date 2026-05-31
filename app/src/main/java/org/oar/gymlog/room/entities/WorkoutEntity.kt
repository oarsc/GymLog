package org.oar.gymlog.room.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "workout")
class WorkoutEntity {
    @PrimaryKey(autoGenerate = true)
    var workoutId = 0
    var name = ""

    class WithWorkoutExercises {
        @Embedded
        var workout: WorkoutEntity? = null

        @Relation(
            parentColumn = "workoutId",
            entityColumn = "workoutId")
        var exercises: List<WorkoutExerciseEntity> = ArrayList()
    }
}