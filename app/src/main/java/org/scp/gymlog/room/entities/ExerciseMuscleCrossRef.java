package org.scp.gymlog.room.entities;

import static androidx.room.ForeignKey.CASCADE;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(
        tableName = "exercise_x_muscle_group",
        primaryKeys = {"exerciseId", "muscleId"},

        foreignKeys = {
                @ForeignKey(
                        entity = ExerciseEntity.class,
                        parentColumns = "exerciseId",
                        childColumns = "exerciseId",
                        onDelete = CASCADE,
                        onUpdate = CASCADE),
                @ForeignKey(
                        entity = MuscleEntity.class,
                        parentColumns = "muscleId",
                        childColumns = "muscleId",
                        onDelete = CASCADE,
                        onUpdate = CASCADE),
        },
        indices = {@Index("exerciseId"), @Index("muscleId")}
)
public  class ExerciseMuscleCrossRef {
    public int exerciseId;
    public int muscleId;
}