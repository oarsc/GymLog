package org.scp.gymlog;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

import org.scp.gymlog.exceptions.LoadException;
import org.scp.gymlog.model.Exercise;
import org.scp.gymlog.room.AppDatabase;
import org.scp.gymlog.room.DBThread;
import org.scp.gymlog.room.EntityMapped;
import org.scp.gymlog.room.entities.MuscleEntity;
import org.scp.gymlog.ui.main.MainActivity;
import org.scp.gymlog.model.Data;
import org.scp.gymlog.model.MuscularGroup;

import java.util.Arrays;
import java.util.List;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (preferences.getBoolean("nightTheme", false)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            recreate();
        }

        initialConfigLoading();
        new DBThread(this, db -> {
                List<MuscleEntity> muscleGroups = db.muscleDao().getOnlyMuscleGroups();
                if (muscleGroups.isEmpty()) {
                    saveInitialData(db);
                } else {
                    loadData(db);
                }
                goMain();
        });
    }

    private void initialConfigLoading() {
        List<MuscularGroup> groups = Data.getInstance().getMuscularGroups();

        groups.clear();
        int id = 0;
        Arrays.asList(
                new MuscularGroup(++id, R.string.group_pectoral, R.drawable.muscle_pectoral),
                new MuscularGroup(++id, R.string.group_upper_back, R.drawable.muscle_upper_back),
                new MuscularGroup(++id, R.string.group_lower_back, R.drawable.muscle_lower_back),
                new MuscularGroup(++id, R.string.group_deltoid, R.drawable.muscle_deltoid),
                new MuscularGroup(++id, R.string.group_trapezius, R.drawable.muscle_trapezius),
                new MuscularGroup(++id, R.string.group_biceps, R.drawable.muscle_biceps),
                new MuscularGroup(++id, R.string.group_triceps, R.drawable.muscle_triceps),
                new MuscularGroup(++id, R.string.group_forearm, R.drawable.muscle_forearm),
                new MuscularGroup(++id, R.string.group_quadriceps, R.drawable.muscle_quadriceps),
                new MuscularGroup(++id, R.string.group_hamstrings, R.drawable.muscle_hamstring),
                new MuscularGroup(++id, R.string.group_calves, R.drawable.muscle_calves),
                new MuscularGroup(++id, R.string.group_glutes, R.drawable.muscle_glutes),
                new MuscularGroup(++id, R.string.group_abdominals, R.drawable.muscle_abdominals),
                new MuscularGroup(++id, R.string.group_cardio, R.drawable.muscle_cardio)
        ).forEach(groups::add);
    }

    private void saveInitialData(AppDatabase db) {
        MuscleEntity[] muscularGroups = Data.getInstance().getMuscularGroups().stream()
                .map(EntityMapped::toEntity)
                .toArray(MuscleEntity[]::new);

        db.muscleDao().insertAll(muscularGroups);
    }

    private void loadData(AppDatabase db) {
        Data data = Data.getInstance();
        List<MuscularGroup> groups = data.getMuscularGroups();

        db.exerciseDao().getAll().stream()
                .map(x -> {
                    Exercise e = new Exercise();
                    e.fromEntity(x.exercise);
                    x.muscleGroups.stream()
                            .map(m -> m.muscleId)
                            .map(id -> groups.stream()
                                    .filter(group -> group.getId() == id)
                                    .findFirst()
                                    .orElseThrow(()->new LoadException("Muscle "+id+" not found in local structure")))
                            .forEach(e.getBelongingMuscularGroups()::add);
                    return e;
                })
                .forEach(data.getExercises()::add);
    }

    public void goMain(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}