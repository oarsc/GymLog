package org.scp.gymlog.room.entities

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
        lateinit var muscle: MuscleEntity

        @Relation(
            parentColumn = "muscleId",
            entityColumn = "exerciseId",
            associateBy = Junction(ExerciseMuscleCrossRef::class))
        var exercises: List<ExerciseEntity> = ArrayList()
    }
}