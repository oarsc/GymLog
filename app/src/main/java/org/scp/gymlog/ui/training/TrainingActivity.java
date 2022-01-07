package org.scp.gymlog.ui.training;

import static org.scp.gymlog.ui.main.history.HistoryFragment.getTrainingData;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.scp.gymlog.R;
import org.scp.gymlog.exceptions.LoadException;
import org.scp.gymlog.model.Bit;
import org.scp.gymlog.model.Muscle;
import org.scp.gymlog.room.AppDatabase;
import org.scp.gymlog.room.entities.BitEntity;
import org.scp.gymlog.room.entities.TrainingEntity;
import org.scp.gymlog.ui.common.DBAppCompatActivity;
import org.scp.gymlog.ui.main.history.TrainingData;
import org.scp.gymlog.util.Data;
import org.scp.gymlog.util.DateUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TrainingActivity extends DBAppCompatActivity {
    private TrainingData trainingData;
    private final List<ExerciseBits> exerciseBits = new ArrayList<>();
    private TrainingRecyclerViewAdapter adapter;
    private LinearLayoutManager linearLayout;

    @Override
    protected int onLoad(Bundle savedInstanceState, AppDatabase db) {
        int trainingId = getIntent().getExtras().getInt("trainingId");
        TrainingEntity training = db.trainingDao().getTraining(trainingId)
                .orElseThrow(() -> new LoadException("Cannot find trainingId: "+trainingId));
        List<BitEntity> bits = db.bitDao().getHistoryByTrainingId(trainingId);
        trainingData = getTrainingData(training, bits);

        for (BitEntity bit : bits) {
            ExerciseBits exerciseBit = exerciseBits.stream()
                    .filter(eb -> eb.getExercise().getId() == bit.exerciseId)
                    .findAny()
                    .orElseGet(() -> {
                        ExerciseBits eb = new ExerciseBits(Data.getExercise(bit.exerciseId));
                        exerciseBits.add(eb);
                        return eb;
                    });

            exerciseBit.getBits().add(new Bit().fromEntity(bit));
        }

        return CONTINUE;
    }

    @Override
    protected void onDelayedCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_training);
        setTitle(R.string.title_training);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean internationalSystem = preferences.getBoolean("internationalSystem", true);

        setHeaderInfo();

        final RecyclerView historyRecyclerView = findViewById(R.id.historyList);
        historyRecyclerView.setLayoutManager(linearLayout = new LinearLayoutManager(this));
        historyRecyclerView.setAdapter(adapter = new TrainingRecyclerViewAdapter(this, exerciseBits, internationalSystem));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.expand_collapse_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.expandButton) {
            adapter.expandAll();
        } else if (item.getItemId() == R.id.collapseButton) {
            adapter.collapseAll();
            linearLayout.scrollToPosition(0);
        }
        return false;
    }

    @SuppressLint("SetTextI18n")
    private void setHeaderInfo() {
        View fragment = findViewById(R.id.fragmentTraining);
        TextView title = findViewById(R.id.title);
        TextView subtitle = findViewById(R.id.subtitle);
        View indicator = findViewById(R.id.indicator);

        fragment.setClickable(false);

        title.setText(getResources().getString(R.string.text_training)
                +" #" + trainingData.getId() + ": "+ getResources().getString(R.string.text_started_at)
                +" " + DateUtils.getTime(trainingData.getStartDate()));

        subtitle.setText(
                trainingData.getMostUsedMuscles().stream()
                        .map(Muscle::getText)
                        .map(getResources()::getString)
                        .collect(Collectors.joining(", ")));

        indicator.setBackgroundResource(trainingData.getMostUsedMuscles().get(0).getColor());
    }
}