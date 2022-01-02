package org.scp.gymlog.room.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import org.scp.gymlog.room.entities.ExerciseMuscleCrossRef;
import org.scp.gymlog.room.entities.SecondaryExerciseMuscleCrossRef;

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

    @Insert
    long insert(SecondaryExerciseMuscleCrossRef exercise);

    @Insert
    long[] insertAll(SecondaryExerciseMuscleCrossRef... exercises);

    @Delete
    void delete(SecondaryExerciseMuscleCrossRef exercise);

    @Query("DELETE FROM secondary_exercise_x_muscle_group WHERE exerciseId = :exerciseId")
    int clearSecondaryMusclesFromExercise(int exerciseId);
}
