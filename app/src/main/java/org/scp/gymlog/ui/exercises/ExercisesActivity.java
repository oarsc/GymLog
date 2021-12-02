package org.scp.gymlog.ui.exercises;

import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.scp.gymlog.R;
import org.scp.gymlog.exceptions.InternalException;
import org.scp.gymlog.model.Data;
import org.scp.gymlog.model.MuscularGroup;
import org.scp.gymlog.ui.tools.BackAppCompatActivity;

public class ExercisesActivity extends BackAppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercises);

        int muscularGroupId = getIntent().getExtras().getInt("muscularGroupId");
        MuscularGroup muscularGroup = Data.getInstance().getMuscularGroups().stream()
                .filter(gr -> gr.getId() == muscularGroupId)
                .findFirst()
                .orElseThrow(() -> new InternalException("Muscular group id not found"));

        setTitle(muscularGroup.getText());
        RecyclerView recyclerView = findViewById(R.id.exercises_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new ExercisesRecyclerViewAdapter(muscularGroup, this));
    }
}