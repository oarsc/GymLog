package org.scp.gymlog.ui.splash;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

import org.scp.gymlog.ui.main.MainActivity;
import org.scp.gymlog.R;
import org.scp.gymlog.model.Data;
import org.scp.gymlog.model.MuscularGroup;
import org.scp.gymlog.service.ContentManager;

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
        ContentManager.loadExercises(this);

        goMain();
    }


    private void initialConfigLoading() {
        List<MuscularGroup> groups = Data.getInstance().getGroups();

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

    public void goMain(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}