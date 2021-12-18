package org.scp.gymlog;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

import org.scp.gymlog.exceptions.LoadException;
import org.scp.gymlog.model.Bar;
import org.scp.gymlog.model.Exercise;
import org.scp.gymlog.room.AppDatabase;
import org.scp.gymlog.room.DBThread;
import org.scp.gymlog.room.entities.MuscleEntity;
import org.scp.gymlog.ui.main.MainActivity;
import org.scp.gymlog.util.Data;
import org.scp.gymlog.model.Muscle;
import org.scp.gymlog.service.InitialDataService;

import java.util.Arrays;
import java.util.List;

public class SplashActivity extends AppCompatActivity {

    private InitialDataService initialDataService = new InitialDataService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (preferences.getBoolean("nightTheme", false)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            recreate();
        }

        defaultInitialConfigLoading();
        new DBThread(this, db -> {
                List<MuscleEntity> muscles = db.muscleDao().getOnlyMuscles();
                if (muscles.isEmpty()) {
                    initialDataService.persist(getAssets(), db);
                }
                loadData(db);
                goMain();
        });
    }

    private void defaultInitialConfigLoading() {
        List<Muscle> muscles = Data.getInstance().getMuscles();
        muscles.clear();
        int muscleId = 0;
        Arrays.asList(
                new Muscle(++muscleId, R.string.group_pectoral, R.drawable.muscle_pectoral),
                new Muscle(++muscleId, R.string.group_upper_back, R.drawable.muscle_upper_back),
                new Muscle(++muscleId, R.string.group_lower_back, R.drawable.muscle_lower_back),
                new Muscle(++muscleId, R.string.group_deltoid, R.drawable.muscle_deltoid),
                new Muscle(++muscleId, R.string.group_trapezius, R.drawable.muscle_trapezius),
                new Muscle(++muscleId, R.string.group_biceps, R.drawable.muscle_biceps),
                new Muscle(++muscleId, R.string.group_triceps, R.drawable.muscle_triceps),
                new Muscle(++muscleId, R.string.group_forearm, R.drawable.muscle_forearm),
                new Muscle(++muscleId, R.string.group_quadriceps, R.drawable.muscle_quadriceps),
                new Muscle(++muscleId, R.string.group_hamstrings, R.drawable.muscle_hamstring),
                new Muscle(++muscleId, R.string.group_calves, R.drawable.muscle_calves),
                new Muscle(++muscleId, R.string.group_glutes, R.drawable.muscle_glutes),
                new Muscle(++muscleId, R.string.group_abdominals, R.drawable.muscle_abdominals),
                new Muscle(++muscleId, R.string.group_cardio, R.drawable.muscle_cardio)
        ).forEach(muscles::add);
    }

    private void loadData(AppDatabase db) {
        Data data = Data.getInstance();

        List<Bar> bars = data.getBars();
        bars.clear();
        db.barDao().getAll().stream()
                .map(x -> new Bar().fromEntity(x))
                .forEach(bars::add);

        List<Muscle> muscles = data.getMuscles();
        List<Exercise> exercises = data.getExercises();
        exercises.clear();
        db.exerciseDao().getAll().stream()
                .map(x -> {
                    Exercise e = new Exercise().fromEntity(x.exercise);
                    x.muscles.stream()
                            .map(m -> m.muscleId)
                            .map(id -> muscles.stream()
                                    .filter(group -> group.getId() == id)
                                    .findFirst()
                                    .orElseThrow(()->new LoadException("Muscle "+id+" not found in local structure")))
                            .forEach(e.getBelongingMuscles()::add);
                    return e;
                })
                .forEach(exercises::add);

        db.trainingDao().getCurrentTraining()
                .ifPresent(training -> data.setTrainingId(training.trainingId));
    }

    public void goMain(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}