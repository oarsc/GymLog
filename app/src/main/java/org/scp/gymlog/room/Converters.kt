package org.scp.gymlog.room

import androidx.room.TypeConverter
import org.scp.gymlog.model.ExerciseType
import org.scp.gymlog.model.WeightSpecification
import org.scp.gymlog.util.DateUtils.timeInMillis
import org.scp.gymlog.util.DateUtils.toLocalDateTime
import java.time.LocalDateTime

object Converters {
    @TypeConverter
    fun toDate(millis: Long?): LocalDateTime? {
        return millis?.toLocalDateTime
    }

    @TypeConverter
    fun fromDate(localDate: LocalDateTime?): Long? {
        return localDate?.timeInMillis
    }

    @TypeConverter
    fun fromWeightSpecification(weightSpecification: WeightSpecification): Short {
        return weightSpecification.ordinal.toShort()
    }

    @TypeConverter
    fun toWeightSpecification(weightSpecification: Short): WeightSpecification {
        return WeightSpecification.values()[weightSpecification.toInt()]
    }

    @TypeConverter
    fun fromExerciseType(exerciseType: ExerciseType): Short {
        return exerciseType.ordinal.toShort()
    }

    @TypeConverter
    fun toExerciseType(exerciseType: Short): ExerciseType {
        return ExerciseType.values()[exerciseType.toInt()]
    }
}