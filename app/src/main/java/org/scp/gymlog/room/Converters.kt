package org.scp.gymlog.room

import androidx.room.TypeConverter
import org.scp.gymlog.model.WeightSpecification
import java.util.*

object Converters {
    @TypeConverter
    fun toDate(dateLong: Long?): Calendar? {
        if (dateLong == null) {
            return null
        }
        val cal = Calendar.getInstance()
        cal.timeInMillis = dateLong
        return cal
    }

    @TypeConverter
    fun fromDate(cal: Calendar?): Long? {
        return cal?.timeInMillis
    }

    @TypeConverter
    fun fromWeightSpecification(weightSpecification: WeightSpecification): Short {
        return weightSpecification.ordinal.toShort()
    }

    @TypeConverter
    fun toWeightSpecification(weightSpecification: Short): WeightSpecification {
        return WeightSpecification.values()[weightSpecification.toInt()]
    }
}