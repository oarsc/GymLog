package org.scp.gymlog.room;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import org.scp.gymlog.room.daos.ExerciseDao;
import org.scp.gymlog.room.daos.ExerciseMuscleCrossRefDao;
import org.scp.gymlog.room.daos.MuscleDao;
import org.scp.gymlog.room.entities.ExerciseEntity;
import org.scp.gymlog.room.entities.ExerciseMuscleCrossRef;
import org.scp.gymlog.room.entities.MuscleEntity;

@Database(entities = {
        ExerciseEntity.class,
        MuscleEntity.class,
        ExerciseMuscleCrossRef.class
}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ExerciseDao exerciseDao();
    public abstract MuscleDao muscleDao();
    public abstract ExerciseMuscleCrossRefDao exerciseMuscleCrossRefDao();
}
