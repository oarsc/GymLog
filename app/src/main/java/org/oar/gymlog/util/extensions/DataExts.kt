package org.oar.gymlog.util.extensions

import org.oar.gymlog.model.WeightPeriod
import org.oar.gymlog.model.WeightPeriodModification
import org.oar.gymlog.room.AppDatabase
import org.oar.gymlog.util.Data
import java.time.LocalDate

object DataExts {
    fun AppDatabase.updateTodayWeightPeriod() {
        Data.weightPeriod = weightDao().getPeriodByDate(LocalDate.now())?.let { entity ->
            WeightPeriod(entity).apply {
                weightDao()
                    .getModificationsByPeriodId(id)
                    .forEach {
                        modifications.add(WeightPeriodModification(it, this))
                    }
            }
        }
    }
}