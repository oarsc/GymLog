package org.oar.gymlog.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "weight")
class WeightEntity {
    @PrimaryKey(autoGenerate = false)
    var date: LocalDate = LocalDate.EPOCH
    var weight: Int = 0
}