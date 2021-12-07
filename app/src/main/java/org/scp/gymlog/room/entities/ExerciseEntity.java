package org.scp.gymlog.room.entities;

import androidx.annotation.NonNull;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.Junction;
import androidx.room.PrimaryKey;
import androidx.room.Relation;

import java.util.Date;
import java.util.List;

@Entity(
        tableName = "exercise",
        indices = @Index({ "exerciseId", "lastTrained" })
)
public class ExerciseEntity {
    @PrimaryKey(autoGenerate = true)
    public int exerciseId;
    @NonNull
    public String name;
    @NonNull
    public String image;
    @NonNull
    public Date lastTrained;
    public int step;
    public boolean requiresBar;

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
