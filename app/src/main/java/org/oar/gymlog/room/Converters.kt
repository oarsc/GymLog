package org.oar.gymlog.room

import androidx.room.TypeConverter
import org.oar.gymlog.model.ExerciseType
import org.oar.gymlog.model.GymRelation
import org.oar.gymlog.model.WeightSpecification
import org.oar.gymlog.util.DateUtils.timeInMillis
import org.oar.gymlog.util.DateUtils.toLocalDateTime
import java.time.LocalDateTime

object Converters {
    @TypeConverter
    fun toDate(millis: Long?): LocalDateTime? =
        millis?.toLocalDateTime

    @TypeConverter
    fun fromDate(localDate: LocalDateTime?): Long? =
        localDate?.timeInMillis

    @TypeConverter
    fun fromWeightSpecification(weightSpecification: WeightSpecification): Short =
        weightSpecification.ordinal.toShort()

    @TypeConverter
    fun toWeightSpecification(weightSpecification: Short): WeightSpecification =
        WeightSpecification.entries[weightSpecification.toInt()]

    @TypeConverter
    fun fromExerciseType(exerciseType: ExerciseType): Short =
        exerciseType.ordinal.toShort()

    @TypeConverter
    fun toExerciseType(exerciseType: Short): ExerciseType =
        ExerciseType.entries[exerciseType.toInt()]

    @TypeConverter
    fun fromGymRelation(gymRelation: GymRelation): Short =
        gymRelation.ordinal.toShort()

    @TypeConverter
    fun toGymRelation(gymRelation: Short): GymRelation =
        GymRelation.entries[gymRelation.toInt()]
}