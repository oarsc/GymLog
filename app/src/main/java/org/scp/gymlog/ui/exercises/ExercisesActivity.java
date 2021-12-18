package org.scp.gymlog.ui.exercises;

import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.scp.gymlog.R;
import org.scp.gymlog.exceptions.InternalException;
import org.scp.gymlog.model.Muscle;
import org.scp.gymlog.room.AppDatabase;
import org.scp.gymlog.ui.common.DBAppCompatActivity;
import org.scp.gymlog.ui.common.components.TrainingFloatingActionButton;
import org.scp.gymlog.util.Data;

import java.util.List;

public class ExercisesActivity extends DBAppCompatActivity {

    private int muscleId;
    private List<Integer> order;

    @Override
    protected int onLoad(Bundle savedInstanceState, AppDatabase db) {
        muscleId = getIntent().getExtras().getInt("muscleId");
        order = db.exerciseDao().getOrderedExercises(muscleId);
        return CONTINUE;
    }

    @Override
    protected void onDelayedCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_exercises);

        Muscle muscle = Data.getInstance().getMuscles().stream()
                .filter(gr -> gr.getId() == muscleId)
                .findFirst()
                .orElseThrow(() -> new InternalException("Muscle id not found"));

        setTitle(muscle.getText());
        RecyclerView recyclerView = findViewById(R.id.exercises_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new ExercisesRecyclerViewAdapter(order, this));

        TrainingFloatingActionButton fab = findViewById(R.id.fab_training);
        fab.updateFloatingActionButton();
    }
}