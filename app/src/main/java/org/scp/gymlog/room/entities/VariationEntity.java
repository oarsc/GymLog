package org.scp.gymlog.room.entities;

import static androidx.room.ForeignKey.CASCADE;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "variation",
        foreignKeys = {
                @ForeignKey(
                        entity = ExerciseEntity.class,
                        parentColumns = "exerciseId",
                        childColumns = "exerciseId",
                        onDelete = CASCADE,
                        onUpdate = CASCADE),
        },
        indices = {
                @Index("variationId"),
                @Index("exerciseId"),
        }
)
public class VariationEntity {
    @PrimaryKey(autoGenerate = true)
    public int variationId;
    @NonNull
    public String name;
    public int exerciseId;
}
