package org.oar.gymlog.room.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.time.LocalDate

@Entity(tableName = "weight_period")
class WeightPeriodEntity {
    @PrimaryKey(autoGenerate = true)
    var weightPeriodId = 0
    var start: LocalDate = LocalDate.EPOCH
    var end: LocalDate = LocalDate.EPOCH
    var initialWeight: Int = 0
    var initialBodyFatPercent: Int = 0
    var gainGramsPerWeek: Int = 0
    var loseGramsPerWeek: Int = 0
    var expectedMuscleGain: Int = 0

    class WithModifications {
        @Embedded
        var weightPeriod: WeightPeriodEntity? = null

        @Relation(
            parentColumn = "weightPeriodId",
            entityColumn = "weightPeriodId")
        var modifications: List<WeightPeriodModificationEntity> = emptyList()
    }
}