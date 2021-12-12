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

import java.util.Date;
import java.util.List;

@Entity(
        tableName = "bit",
        foreignKeys = {
                @ForeignKey(
                        entity = ExerciseEntity.class,
                        parentColumns = "exerciseId",
                        childColumns = "exerciseId",
                        onDelete = CASCADE,
                        onUpdate = CASCADE),
                @ForeignKey(
                        entity = TrainingEntity.class,
                        parentColumns = "trainingId",
                        childColumns = "trainingId",
                        onDelete = CASCADE,
                        onUpdate = CASCADE),
        },
        indices = {
                @Index("exerciseId"),
                @Index("trainingId"),
                @Index({ "exerciseId", "timestamp" }),
                @Index({ "exerciseId", "trainingId", "timestamp" }),
        }
)
public class BitEntity {
    @PrimaryKey(autoGenerate = true)
    public int bitId;
    public int exerciseId;
    public int trainingId;
    public int reps;
    public int totalWeight;
    public boolean kilos;
    @NonNull
    public String note;
    @NonNull
    public Date timestamp;

    public static class WithBar {
        @Embedded
        public BitEntity bit;
        @Relation(
                parentColumn = "barId",
                entityColumn = "barId"
        )
        public BarEntity bar;
    }
}
