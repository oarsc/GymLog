package org.oar.gymlog.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.oar.gymlog.room.daos.BarDao
import org.oar.gymlog.room.daos.BitDao
import org.oar.gymlog.room.daos.ExerciseDao
import org.oar.gymlog.room.daos.ExerciseMuscleCrossRefDao
import org.oar.gymlog.room.daos.GymDao
import org.oar.gymlog.room.daos.MuscleDao
import org.oar.gymlog.room.daos.TrainingDao
import org.oar.gymlog.room.daos.VariationDao
import org.oar.gymlog.room.entities.BarEntity
import org.oar.gymlog.room.entities.BitEntity
import org.oar.gymlog.room.entities.ExerciseEntity
import org.oar.gymlog.room.entities.ExerciseMuscleCrossRef
import org.oar.gymlog.room.entities.GymEntity
import org.oar.gymlog.room.entities.MuscleEntity
import org.oar.gymlog.room.entities.SecondaryExerciseMuscleCrossRef
import org.oar.gymlog.room.entities.TrainingEntity
import org.oar.gymlog.room.entities.VariationEntity

@TypeConverters(Converters::class)
@Database(
    entities = [
        GymEntity::class,
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
    abstract fun gymDao(): GymDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun muscleDao(): MuscleDao
    abstract fun exerciseMuscleCrossRefDao(): ExerciseMuscleCrossRefDao
    abstract fun bitDao(): BitDao
    abstract fun barDao(): BarDao
    abstract fun trainingDao(): TrainingDao
    abstract fun variationDao(): VariationDao
}