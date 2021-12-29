package org.scp.gymlog;

import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

import org.scp.gymlog.exceptions.LoadException;
import org.scp.gymlog.model.Bar;
import org.scp.gymlog.model.Exercise;
import org.scp.gymlog.model.Muscle;
import org.scp.gymlog.room.AppDatabase;
import org.scp.gymlog.room.DBThread;
import org.scp.gymlog.room.entities.MuscleEntity;
import org.scp.gymlog.service.InitialDataService;
import org.scp.gymlog.ui.main.MainActivity;
import org.scp.gymlog.util.Data;

import java.util.List;

public class SplashActivity extends AppCompatActivity {

    private final InitialDataService initialDataService = new InitialDataService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (preferences.getBoolean("nightTheme", false) &&
                AppCompatDelegate.getDefaultNightMode() != MODE_NIGHT_YES) {
            AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES);
            recreate();

        } else {
            DBThread.run(this, db -> {
                List<MuscleEntity> muscles = db.muscleDao().getOnlyMuscles();
                if (muscles.isEmpty()) {
                    initialDataService.persist(getAssets(), db);
                }
                loadData(db);
                goMain();
            });
        }
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