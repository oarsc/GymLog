package org.scp.gymlog.room.entities;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "bar",
        indices = {
                @Index("weight"),
        }
)
public class BarEntity {
    @PrimaryKey(autoGenerate = true)
    public int barId;
    public int weight;
    public boolean internationalSystem;
}
