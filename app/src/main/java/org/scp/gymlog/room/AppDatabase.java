package org.scp.gymlog.room;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import org.scp.gymlog.room.daos.BarDao;
import org.scp.gymlog.room.daos.BitDao;
import org.scp.gymlog.room.daos.ExerciseDao;
import org.scp.gymlog.room.daos.ExerciseMuscleCrossRefDao;
import org.scp.gymlog.room.daos.MuscleDao;
import org.scp.gymlog.room.entities.BarEntity;
import org.scp.gymlog.room.entities.BitEntity;
import org.scp.gymlog.room.entities.ExerciseEntity;
import org.scp.gymlog.room.entities.ExerciseMuscleCrossRef;
import org.scp.gymlog.room.entities.MuscleEntity;

@TypeConverters({Converters.class})
@Database(entities = {
        ExerciseEntity.class,
        MuscleEntity.class,
        ExerciseMuscleCrossRef.class,
        BitEntity.class,
        BarEntity.class,
}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ExerciseDao exerciseDao();
    public abstract MuscleDao muscleDao();
    public abstract ExerciseMuscleCrossRefDao exerciseMuscleCrossRefDao();
    public abstract BitDao bitDao();
    public abstract BarDao barDao();
}
