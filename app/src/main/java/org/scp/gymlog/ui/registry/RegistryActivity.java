package org.scp.gymlog.ui.registry;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import org.scp.gymlog.R;
import org.scp.gymlog.exceptions.InternalException;
import org.scp.gymlog.util.Data;
import org.scp.gymlog.model.Exercise;
import org.scp.gymlog.ui.tools.BackAppCompatActivity;

public class RegistryActivity extends BackAppCompatActivity {

    private Exercise exercise;
    private EditText weight;
    private EditText reps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registry);
        setTitle(R.string.title_registry);

        int exerciseId = getIntent().getExtras().getInt("exerciseId");
        exercise = Data.getInstance().getExercises().stream()
                .filter(ex -> ex.getId() == exerciseId)
                .findFirst()
                .orElseThrow(() -> new InternalException("Exercise id not found"));

        TextView title = findViewById(R.id.exerciseName);
        title.setText(exercise.getName());

        weight = findViewById(R.id.editWeight);
        reps = findViewById(R.id.editReps);

        modifyEditText(weight, findViewById(R.id.addWeight), true, false);
        modifyEditText(weight, findViewById(R.id.subWeight), false, false);
        modifyEditText(reps, findViewById(R.id.addReps), true, true);
        modifyEditText(reps, findViewById(R.id.subReps), false, true);
    }

    private void modifyEditText(EditText editText, CardView cardView, boolean addition, boolean single) {
        cardView.setOnClickListener(v -> {
            int value = 0;
            try {
                value = Integer.parseInt(editText.getText().toString());
            } catch (NumberFormatException e) {}

            int step = single? 1 : 1;
            value += addition? step : -step;
            editText.setText(String.valueOf(value));
        });
    }
}