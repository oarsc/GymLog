package org.scp.gymlog.ui.exercises;

import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.scp.gymlog.R;
import org.scp.gymlog.exceptions.InternalException;
import org.scp.gymlog.util.Data;
import org.scp.gymlog.model.Muscle;
import org.scp.gymlog.ui.common.BackAppCompatActivity;

public class ExercisesActivity extends BackAppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercises);

        int muscleId = getIntent().getExtras().getInt("muscleId");
        Muscle muscle = Data.getInstance().getMuscles().stream()
                .filter(gr -> gr.getId() == muscleId)
                .findFirst()
                .orElseThrow(() -> new InternalException("Muscle id not found"));

        setTitle(muscle.getText());
        RecyclerView recyclerView = findViewById(R.id.exercises_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new ExercisesRecyclerViewAdapter(muscle, this));
    }
}