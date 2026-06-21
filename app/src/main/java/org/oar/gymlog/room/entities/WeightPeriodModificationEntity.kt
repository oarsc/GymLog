package org.oar.gymlog.room.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "weight_period_modification",
    foreignKeys = [
        ForeignKey(
            entity = WeightPeriodEntity::class,
            parentColumns = ["weightPeriodId"],
            childColumns = ["weightPeriodId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE),
    ],
    indices = [
        Index("weightPeriodId")
    ]
)
class WeightPeriodModificationEntity {
    @PrimaryKey(autoGenerate = true)
    var weightPeriodModificationId = 0
    var weightPeriodId = 0
    var start: LocalDate = LocalDate.EPOCH
    var end: LocalDate = LocalDate.EPOCH
    var gramsPerWeek: Int = 0
}