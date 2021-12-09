package org.scp.gymlog.room.entities;

import static androidx.room.ForeignKey.CASCADE;
import static androidx.room.ForeignKey.SET_NULL;

import androidx.annotation.NonNull;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.Junction;
import androidx.room.PrimaryKey;
import androidx.room.Relation;

import org.scp.gymlog.model.WeightSpecification;

import java.util.Date;
import java.util.List;

@Entity(
        tableName = "exercise",
        foreignKeys = {
                @ForeignKey(
                        entity = BarEntity.class,
                        parentColumns = "barId",
                        childColumns = "lastBarId",
                        onDelete = SET_NULL,
                        onUpdate = CASCADE),
        },
        indices = {
                @Index("lastBarId"),
                @Index({ "exerciseId", "lastTrained" })
        }
)
public class ExerciseEntity {
    @PrimaryKey(autoGenerate = true)
    public int exerciseId;
    @NonNull
    public String name;
    @NonNull
    public String image;
    public boolean requiresBar;

    // Last configs
    @NonNull
    public Date lastTrained;
    @NonNull
    public WeightSpecification lastWeightSpec;
    public int lastStep = 500;
    public Integer lastBarId;

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
