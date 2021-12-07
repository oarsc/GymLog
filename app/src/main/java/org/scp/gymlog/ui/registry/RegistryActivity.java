package org.scp.gymlog.ui.registry;

import static org.scp.gymlog.util.FormatUtils.ONE_THOUSAND;
import static org.scp.gymlog.util.FormatUtils.toBigDecimal;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputFilter;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.preference.PreferenceManager;

import org.scp.gymlog.R;
import org.scp.gymlog.exceptions.InternalException;
import org.scp.gymlog.model.Bar;
import org.scp.gymlog.model.Weight;
import org.scp.gymlog.room.DBThread;
import org.scp.gymlog.ui.common.dialogs.EditNumberDialogFragment;
import org.scp.gymlog.ui.common.dialogs.EditWeightFormDialogFragment;
import org.scp.gymlog.ui.common.dialogs.model.WeightFormData;
import org.scp.gymlog.util.Data;
import org.scp.gymlog.model.Exercise;
import org.scp.gymlog.ui.common.BackAppCompatActivity;
import org.scp.gymlog.util.FormatUtils;

import java.math.BigDecimal;

public class RegistryActivity extends BackAppCompatActivity {

    private Exercise exercise;
    private EditText weight;
    private EditText reps;

    private Bar barSelected;
    private boolean usingInternationalSystem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registry);
        setTitle(R.string.title_registry);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        usingInternationalSystem = preferences.getBoolean("internationalSystem", false);

        int exerciseId = getIntent().getExtras().getInt("exerciseId");
        exercise = Data.getInstance().getExercises().stream()
                .filter(ex -> ex.getId() == exerciseId)
                .findFirst()
                .orElseThrow(() -> new InternalException("Exercise id not found"));

        TextView title = findViewById(R.id.exerciseName);
        title.setText(exercise.getName());

        weight = findViewById(R.id.editWeight);
        reps = findViewById(R.id.editReps);

        weight.setFilters(new InputFilter[] {(source, start, end, dest, dstart, dend) -> {
            BigDecimal input = FormatUtils.toBigDecimal(dest.toString() + source.toString());
            return input.compareTo(ONE_THOUSAND) < 0 && input.scale() < 3? null : "";
        }});

        modifyEditText(weight, findViewById(R.id.addWeight), true, false);
        modifyEditText(weight, findViewById(R.id.subWeight), false, false);
        modifyEditText(reps, findViewById(R.id.addReps), true, true);
        modifyEditText(reps, findViewById(R.id.subReps), false, true);

        weight.setOnClickListener(v -> showWeightDialog(weight));
        reps.setOnClickListener(view -> {
            EditNumberDialogFragment dialog = new EditNumberDialogFragment(R.string.text_reps,
                    result -> reps.setText(result.toString()));
            dialog.setInitialValue(reps.getText().toString());
            dialog.show(getSupportFragmentManager(), null);
        });

        TextView unitTextView = findViewById(R.id.unit);
        unitTextView.setText(usingInternationalSystem?
                R.string.text_kg :
                R.string.text_lb);
    }

    private void modifyEditText(EditText editText, CardView cardView, boolean addition, boolean single) {
        cardView.setOnClickListener(v -> {
            BigDecimal value = toBigDecimal(editText.getText().toString());

            BigDecimal step = single? BigDecimal.ONE : BigDecimal.ONE;
            if (!addition) step = step.negate();

            value = value.add(step);
            if (value.compareTo(BigDecimal.ZERO) <= 0)value = BigDecimal.ZERO;
            editText.setText(FormatUtils.toString(value));
        });
    }

    private void showWeightDialog(EditText weightEditText) {
        WeightFormData weightFormData = new WeightFormData();

        Weight weight = new Weight(
                toBigDecimal(weightEditText.getText().toString()),
                usingInternationalSystem
        );
        weightFormData.setWeight(weight);
        weightFormData.setExercise(exercise);

        EditWeightFormDialogFragment dialog = new EditWeightFormDialogFragment(R.string.text_weight,
                result -> {
                    weightEditText.setText(FormatUtils.toString(result.getWeight().getValue()));
                    if (result.isExerciseUpdated()) {
                        new DBThread(this, db ->
                            db.exerciseDao().update(exercise.toEntity())
                        );
                    }
                },
                () -> {}, weightFormData);
        dialog.show(getSupportFragmentManager(), null);
    }
}