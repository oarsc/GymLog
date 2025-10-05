package org.oar.gymlog.room.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "muscle")
class MuscleEntity {
    @PrimaryKey
    var muscleId = 0

    class WithExercises {
        @Embedded
        var muscle: MuscleEntity? = null

        @Relation(
            parentColumn = "muscleId",
            entityColumn = "exerciseId",
            associateBy = Junction(ExerciseMuscleCrossRef::class))
        var exercises: List<ExerciseEntity> = ArrayList()
    }
}