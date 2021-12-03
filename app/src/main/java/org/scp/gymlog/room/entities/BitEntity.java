package org.scp.gymlog.room.entities;

import static androidx.room.ForeignKey.CASCADE;

import androidx.room.ColumnInfo;
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
        },
        indices = {
                @Index("exerciseId"),
                @Index({ "exerciseId", "timestamp" })
        }
)
public class BitEntity {
    @PrimaryKey(autoGenerate = true)
    public int bitId;
    public int exerciseId;
    public int reps;
    public int weight;
    public boolean kilos;
    public String note;
    public Date timestamp;
}
