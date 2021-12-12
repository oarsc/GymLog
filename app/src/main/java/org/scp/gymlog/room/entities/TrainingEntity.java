package org.scp.gymlog.room.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Calendar;

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
    public Calendar start;
    public Calendar end;
}
