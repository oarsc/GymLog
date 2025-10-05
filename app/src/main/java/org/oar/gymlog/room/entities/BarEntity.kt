package org.oar.gymlog.room.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "bar",
    indices = [
        Index("weight")
    ]
)
class BarEntity {
    @PrimaryKey(autoGenerate = true)
    var barId = 0
    var weight = 0
    var internationalSystem = false
}