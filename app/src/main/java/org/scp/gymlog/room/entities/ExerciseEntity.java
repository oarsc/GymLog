package org.scp.gymlog.room.entities;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Junction;
import androidx.room.PrimaryKey;
import androidx.room.Relation;

import java.util.List;

@Entity(tableName = "exercise")
public class ExerciseEntity {
    @PrimaryKey(autoGenerate = true)
    public int exerciseId;
    public String name;
    public String image;

    public static class WithMuscles {
        @Embedded
        public ExerciseEntity exercise;
        @Relation(
                parentColumn = "exerciseId",
                entityColumn = "muscleId",
                associateBy = @Junction(ExerciseMuscleCrossRef.class)
        )
        public List<MuscleEntity> muscles;
    }
}
