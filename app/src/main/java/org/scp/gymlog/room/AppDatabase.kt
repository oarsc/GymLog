package org.scp.gymlog.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.scp.gymlog.room.daos.BarDao
import org.scp.gymlog.room.daos.BitDao
import org.scp.gymlog.room.daos.BitNoteCrossRefDao
import org.scp.gymlog.room.daos.ExerciseDao
import org.scp.gymlog.room.daos.ExerciseMuscleCrossRefDao
import org.scp.gymlog.room.daos.GymDao
import org.scp.gymlog.room.daos.MuscleDao
import org.scp.gymlog.room.daos.NoteDao
import org.scp.gymlog.room.daos.TrainingDao
import org.scp.gymlog.room.daos.VariationDao
import org.scp.gymlog.room.entities.BarEntity
import org.scp.gymlog.room.entities.BitEntity
import org.scp.gymlog.room.entities.BitNoteCrossRef
import org.scp.gymlog.room.entities.ExerciseEntity
import org.scp.gymlog.room.entities.ExerciseMuscleCrossRef
import org.scp.gymlog.room.entities.GymEntity
import org.scp.gymlog.room.entities.MuscleEntity
import org.scp.gymlog.room.entities.NoteEntity
import org.scp.gymlog.room.entities.SecondaryExerciseMuscleCrossRef
import org.scp.gymlog.room.entities.TrainingEntity
import org.scp.gymlog.room.entities.VariationEntity

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
        NoteEntity::class,
        BitNoteCrossRef::class,
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
    abstract fun noteDao(): NoteDao
    abstract fun bitNoteCrossRefDao(): BitNoteCrossRefDao
    abstract fun barDao(): BarDao
    abstract fun trainingDao(): TrainingDao
    abstract fun variationDao(): VariationDao
}