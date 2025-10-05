package org.oar.gymlog.room.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import org.oar.gymlog.util.JsonUtils.NoJsonify

@Entity(
    tableName = "gym",
    indices = [
        Index("gymId"),
    ]
)
data class GymEntity(
    @NoJsonify
    @PrimaryKey(autoGenerate = true)
    var gymId: Int = 0,
    var name: String
)