package org.scp.gymlog.room.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(
        tableName = "training",
        indices = {
                @Index({ "start", "end" }),
        }
)
public class TrainingEntity {
    @PrimaryKey(autoGenerate = true)
    public int trainingId;
    @NonNull
    public Date start;
    public Date end;
}
