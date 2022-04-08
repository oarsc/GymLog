package org.scp.gymlog.room.entities

import androidx.room.*

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