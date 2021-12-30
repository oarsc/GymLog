package org.scp.gymlog.room.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import org.scp.gymlog.room.entities.ExerciseMuscleCrossRef;

@Dao
public interface ExerciseMuscleCrossRefDao {
    @Insert
    long insert(ExerciseMuscleCrossRef exercise);

    @Insert
    long[] insertAll(ExerciseMuscleCrossRef... exercises);

    @Delete
    void delete(ExerciseMuscleCrossRef exercise);

    @Query("DELETE FROM exercise_x_muscle_group WHERE exerciseId = :exerciseId")
    int clearMusclesFromExercise(int exerciseId);
}
