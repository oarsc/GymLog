package org.scp.gymlog.ui.training;

import static org.scp.gymlog.ui.main.history.HistoryFragment.getTrainingData;
import static org.scp.gymlog.util.LambdaUtils.valueEquals;

import android.annotation.SuppressLint;
import android.content.Intent;
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
import org.scp.gymlog.model.Variation;
import org.scp.gymlog.room.AppDatabase;
import org.scp.gymlog.room.entities.BitEntity;
import org.scp.gymlog.room.entities.TrainingEntity;
import org.scp.gymlog.ui.common.DBAppCompatActivity;
import org.scp.gymlog.ui.common.dialogs.EditExercisesLastsDialogFragment;
import org.scp.gymlog.ui.main.history.TrainingData;
import org.scp.gymlog.ui.training.rows.ITrainingRow;
import org.scp.gymlog.ui.training.rows.TrainingBitRow;
import org.scp.gymlog.ui.training.rows.TrainingHeaderRow;
import org.scp.gymlog.ui.training.rows.TrainingVariationRow;
import org.scp.gymlog.util.Data;
import org.scp.gymlog.util.DateUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TrainingActivity extends DBAppCompatActivity {
    private TrainingData trainingData;
    private final List<ExerciseRows> exerciseRows = new ArrayList<>();
    private TrainingMainRecyclerViewAdapter adapter;
    private LinearLayoutManager linearLayout;

    @Override
    protected int onLoad(Bundle savedInstanceState, AppDatabase db) {
        int trainingId = getIntent().getExtras().getInt("trainingId");
        TrainingEntity training = db.trainingDao().getTraining(trainingId)
                .orElseThrow(() -> new LoadException("Cannot find trainingId: "+trainingId));
        List<BitEntity> bits = db.bitDao().getHistoryByTrainingId(trainingId);
        trainingData = getTrainingData(training, bits);

        for (BitEntity bit : bits) {
            ExerciseRows exerciseRow = exerciseRows.stream()
                    .filter(eb -> eb.getExercise().getId() == bit.exerciseId)
                    .findAny()
                    .orElseGet(() -> {
                        ExerciseRows eb = new ExerciseRows(Data.getExercise(bit.exerciseId));
                        exerciseRows.add(eb);
                        return eb;
                    });

            int lastVar = getLastVar(exerciseRow);
            int var = bit.variationId == null? 0 : bit.variationId;
            if (var != lastVar) {
                if (var > 0) {
                    Variation variation = Data.getVariation(exerciseRow.getExercise(), var);
                    exerciseRow.add(new TrainingVariationRow(variation));
                } else if (lastVar != -1) {
                    exerciseRow.add(new TrainingVariationRow(null));
                }
                exerciseRow.add(new TrainingHeaderRow());
            }
            exerciseRow.add(new TrainingBitRow(new Bit().fromEntity(bit)));
        }

        return CONTINUE;
    }

    private int getLastVar(ExerciseRows exerciseRow) {
        if (exerciseRow.isEmpty()) return -1;

        int i = exerciseRow.size();
        boolean found;
        ITrainingRow row;
        do {
            row = exerciseRow.get(--i);
            found = row instanceof TrainingVariationRow;
        } while (!found && i > 0);

        if (found) {
            TrainingVariationRow vRow = (TrainingVariationRow) row;
            Variation variation = vRow.getVariation();
            return variation==null? 0 : vRow.getVariation().getId();
        }
        return 0;
    }

    @Override
    protected void onDelayedCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_training);
        setTitle(R.string.title_training);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean internationalSystem = preferences.getBoolean("internationalSystem", true);

        setHeaderInfo();

        int focusBit = getIntent().getExtras().getInt("focusBit", -1);
        final int focusElement;
        if (focusBit >= 0) {
            int index = 0;
            for (ExerciseRows exerciseRow : exerciseRows) {
                if (exerciseRow.stream()
                        .filter(row -> row instanceof TrainingBitRow)
                        .map(row -> (TrainingBitRow) row)
                        .map(TrainingBitRow::getBit)
                        .map(Bit::getId)
                        .anyMatch(valueEquals(focusBit))) {
                    break;
                }
                index++;
            }
            focusElement = index < exerciseRows.size()? index : -1;
        } else {
            focusElement = -1;
        }

        final RecyclerView historyRecyclerView = findViewById(R.id.historyList);
        historyRecyclerView.setLayoutManager(linearLayout = new LinearLayoutManager(this));
        historyRecyclerView.setAdapter(adapter = new TrainingMainRecyclerViewAdapter(exerciseRows,
                internationalSystem, focusElement));

        if (focusElement >= 0) {
            linearLayout.scrollToPositionWithOffset(focusElement, 60);
        }

        adapter.setOnLongClickListener(exerciseRow -> {
            EditExercisesLastsDialogFragment dialog = new EditExercisesLastsDialogFragment(R.string.title_exercises,
                    val -> {
                        if (val != null) {
                            Intent data = new Intent();
                            data.putExtra("refresh", true);
                            setResult(RESULT_OK, data);

                            final int index = exerciseRows.indexOf(exerciseRow);
                            runOnUiThread(() -> adapter.notifyItemChanged(index));
                        }
                    },
                    ()->{},
                    exerciseRow.getExercise(), internationalSystem);

            dialog.show(getSupportFragmentManager(), null);
        });

        adapter.setOnBitChangedListener(bit -> {
            Intent data = new Intent();
            data.putExtra("refresh", true);
            setResult(RESULT_OK, data);
        });
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
                +" #" + trainingData.getId() + " "+ getResources().getString(R.string.text_on_smallcaps)
                +" " + DateUtils.getDate(trainingData.getStartDate()));

        subtitle.setText(
                trainingData.getMostUsedMuscles().stream()
                        .map(Muscle::getText)
                        .map(getResources()::getString)
                        .collect(Collectors.joining(", ")));

        indicator.setBackgroundResource(trainingData.getMostUsedMuscles().get(0).getColor());
    }
}