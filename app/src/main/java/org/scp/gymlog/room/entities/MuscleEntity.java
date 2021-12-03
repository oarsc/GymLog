package org.scp.gymlog.room.entities;

import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Junction;
import androidx.room.PrimaryKey;
import androidx.room.Relation;

import java.util.List;

@Entity(tableName = "muscle")
public class MuscleEntity {
    @PrimaryKey
    public int muscleId;

    public static class WithExercises {
        @Embedded
        public MuscleEntity muscleGroup;
        @Relation(
                parentColumn = "muscleId",
                entityColumn = "exerciseId",
                associateBy = @Junction(ExerciseMuscleCrossRef.class)
        )
        public List<ExerciseEntity> exercises;
    }
}
