package org.scp.gymlog.ui.registry;

import static org.scp.gymlog.util.Constants.DATE_ZERO;
import static org.scp.gymlog.util.Constants.ONE_THOUSAND;
import static org.scp.gymlog.util.Constants.TWO;
import static org.scp.gymlog.util.FormatUtils.toBigDecimal;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.preference.PreferenceManager;

import org.scp.gymlog.R;
import org.scp.gymlog.exceptions.InternalException;
import org.scp.gymlog.model.Bit;
import org.scp.gymlog.model.Exercise;
import org.scp.gymlog.model.Weight;
import org.scp.gymlog.room.AppDatabase;
import org.scp.gymlog.room.DBThread;
import org.scp.gymlog.room.entities.BitEntity;
import org.scp.gymlog.ui.common.BackDBAppCompatActivity;
import org.scp.gymlog.ui.common.NumberModifierView;
import org.scp.gymlog.ui.common.dialogs.EditNumberDialogFragment;
import org.scp.gymlog.ui.common.dialogs.EditWeightFormDialogFragment;
import org.scp.gymlog.ui.common.dialogs.model.WeightFormData;
import org.scp.gymlog.util.Data;
import org.scp.gymlog.util.FormatUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class RegistryActivity extends BackDBAppCompatActivity {
    private static final int LOG_PAGES_SIZE = 10;

    private Exercise exercise;
    private EditText weight;
    private EditText reps;
    private NumberModifierView weightModifier;
    private ImageView weightSpecIcon;
    private ImageView warningIcon;

    private boolean usingInternationalSystem;
    private final List<Bit> log = new ArrayList<>();

    @Override
    protected void onLoad(Bundle savedInstanceState, AppDatabase db) {
        int exerciseId = getIntent().getExtras().getInt("exerciseId");
        exercise = Data.getInstance().getExercises().stream()
                .filter(ex -> ex.getId() == exerciseId)
                .findFirst()
                .orElseThrow(() -> new InternalException("Exercise id not found"));

        List<BitEntity> log = db.bitDao().getHistory(exerciseId, DATE_ZERO, LOG_PAGES_SIZE);
        if (log.size() == LOG_PAGES_SIZE && log.get(0).trainingId == log.get(LOG_PAGES_SIZE-1).trainingId) {
            log = db.bitDao().getHistory(exerciseId, log.get(0).trainingId);
        }
        log.stream()
                .map(bit -> new Bit().fromEntity(bit))
                .forEach(this.log::add);
    }

    @Override
    protected void onDelayedCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_registry);
        setTitle(R.string.title_registry);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        usingInternationalSystem = preferences.getBoolean("internationalSystem", false);


        TextView title = findViewById(R.id.exerciseName);
        title.setText(exercise.getName());

        // Weight and Reps Input fields:
        weight = findViewById(R.id.editWeight);
        weight.setFilters(new InputFilter[] {(source, start, end, dest, dstart, dend) -> {
            BigDecimal input = FormatUtils.toBigDecimal(dest.toString() + source.toString());
            return input.compareTo(ONE_THOUSAND) < 0 && input.scale() < 3? null : "";
        }});
        weight.setOnClickListener(v -> showWeightDialog(weight));

        reps = findViewById(R.id.editReps);
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

        weightModifier = findViewById(R.id.weightModifier);
        weightModifier.setStep(exercise.getStep());

        weightSpecIcon = findViewById(R.id.weight_spec_icon);
        weightSpecIcon.setImageResource(exercise.getWeightSpec().icon);

        warningIcon = findViewById(R.id.warning);
        if (exercise.isRequiresBar() == (exercise.getBar() == null)) {
            warningIcon.setVisibility(View.VISIBLE);
        } else {
            warningIcon.setVisibility(View.INVISIBLE);
        }

        loadHistory();
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
                        weightModifier.setStep(exercise.getStep());
                        weightSpecIcon.setImageResource(exercise.getWeightSpec().icon);
                        if (exercise.isRequiresBar() == (exercise.getBar() == null)) {
                            warningIcon.setVisibility(View.VISIBLE);
                        } else {
                            warningIcon.setVisibility(View.INVISIBLE);
                        }

                        new DBThread(this, db ->
                            db.exerciseDao().update(exercise.toEntity())
                        );
                    }
                },
                () -> {}, weightFormData);
        dialog.show(getSupportFragmentManager(), null);
    }

    private void loadHistory() {
        if (!log.isEmpty()) {
            Bit bit = log.get(0);
            reps.setText(String.valueOf(bit.getReps()));
            weight.setText(FormatUtils.toString(
                    getWeightFromTotal(bit.getWeight().getValue(usingInternationalSystem)))
                );
        }
    }

    private BigDecimal getWeightFromTotal(BigDecimal total) {
        switch (exercise.getWeightSpec()) {
            case ONE_SIDE_WEIGHT:
                if (exercise.getBar() != null) {
                    return total.subtract(
                            exercise.getBar().getWeight().getValue(usingInternationalSystem)
                        ).divide(TWO, 2, RoundingMode.HALF_UP);
                }
                return total.divide(TWO, 2, RoundingMode.HALF_UP);
            case NO_BAR_WEIGHT:
                if (exercise.getBar() != null) {
                    return total.subtract(
                            exercise.getBar().getWeight().getValue(usingInternationalSystem)
                        );
                }
        }
        return total;
    }
}