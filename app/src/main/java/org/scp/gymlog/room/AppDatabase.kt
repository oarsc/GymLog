package org.scp.gymlog.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.scp.gymlog.room.daos.*
import org.scp.gymlog.room.entities.*

@TypeConverters(Converters::class)
@Database(
    entities = [
        ExerciseEntity::class,
        VariationEntity::class,
        MuscleEntity::class,
        ExerciseMuscleCrossRef::class,
        SecondaryExerciseMuscleCrossRef::class,
        BitEntity::class,
        BarEntity::class,
        TrainingEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun muscleDao(): MuscleDao
    abstract fun exerciseMuscleCrossRefDao(): ExerciseMuscleCrossRefDao
    abstract fun bitDao(): BitDao
    abstract fun barDao(): BarDao
    abstract fun trainingDao(): TrainingDao
    abstract fun variationDao(): VariationDao
}