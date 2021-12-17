package org.scp.gymlog.ui.registry;

import static org.scp.gymlog.util.Constants.ONE_THOUSAND;
import static org.scp.gymlog.util.FormatUtils.toBigDecimal;
import static org.scp.gymlog.util.FormatUtils.toInt;
import static org.scp.gymlog.util.WeightUtils.getTotalWeight;
import static org.scp.gymlog.util.WeightUtils.getWeightFromTotal;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.scp.gymlog.R;
import org.scp.gymlog.exceptions.InternalException;
import org.scp.gymlog.model.Bit;
import org.scp.gymlog.model.Exercise;
import org.scp.gymlog.model.Weight;
import org.scp.gymlog.room.AppDatabase;
import org.scp.gymlog.room.DBThread;
import org.scp.gymlog.room.entities.BitEntity;
import org.scp.gymlog.room.entities.TrainingEntity;
import org.scp.gymlog.ui.common.DBAppCompatActivity;
import org.scp.gymlog.ui.common.components.NumberModifierView;
import org.scp.gymlog.ui.common.dialogs.EditNotesDialogFragment;
import org.scp.gymlog.ui.common.dialogs.EditNumberDialogFragment;
import org.scp.gymlog.ui.common.dialogs.EditTextDialogFragment;
import org.scp.gymlog.ui.common.dialogs.EditWeightFormDialogFragment;
import org.scp.gymlog.ui.common.dialogs.model.WeightFormData;
import org.scp.gymlog.util.Data;
import org.scp.gymlog.util.FormatUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

public class RegistryActivity extends DBAppCompatActivity {
    private static final int LOG_PAGES_SIZE = 16;

    private Exercise exercise;
    private EditText weight;
    private EditText reps;
    private EditText notes;
    private NumberModifierView weightModifier;
    private ImageView weightSpecIcon;
    private ImageView warningIcon;
    private LogRecyclerViewAdapter recyclerViewAdapter;

    private boolean usingInternationalSystem;
    private final List<Bit> log = new ArrayList<>();
    private int trainingId;

    @Override
    protected int onLoad(Bundle savedInstanceState, AppDatabase db) {
        Optional<TrainingEntity> training = db.trainingDao().getCurrentTraining();
        if (!training.isPresent()) {
            return R.string.validation_training_not_started;
        }
        trainingId = training.get().trainingId;

        int exerciseId = getIntent().getExtras().getInt("exerciseId");
        exercise = Data.getInstance().getExercises().stream()
                .filter(ex -> ex.getId() == exerciseId)
                .findFirst()
                .orElseThrow(() -> new InternalException("Exercise id not found"));

        List<BitEntity> log = db.bitDao().getHistory(exerciseId, LOG_PAGES_SIZE);
        log.stream()
                .map(bit -> new Bit().fromEntity(bit))
                .forEach(this.log::add);

        return CONTINUE;
    }

    @Override
    protected void onDelayedCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_registry);
        setTitle(R.string.title_registry);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        usingInternationalSystem = preferences.getBoolean("internationalSystem", false);


        TextView title = findViewById(R.id.exerciseName);
        title.setText(exercise.getName());

        // Logs:
        RecyclerView recyclerView = findViewById(R.id.log_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(recyclerViewAdapter = new LogRecyclerViewAdapter(log, exercise,
                trainingId));
        recyclerViewAdapter.setOnClickElementListener(this::onClickBit);

        // Save bit log
        findViewById(R.id.confirm).setOnClickListener(this::saveBitLog);

        // Weight and Reps Input fields:
        notes = findViewById(R.id.editNotes);
        notes.setOnClickListener(view -> {
            EditNotesDialogFragment dialog = new EditNotesDialogFragment(R.string.text_notes,
                    exercise.getId(),
                    result -> notes.setText(result.toString()));
            dialog.setInitialValue(notes.getText().toString());
            dialog.show(getSupportFragmentManager(), null);
        });

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
        weightFormData.setStep(exercise.getStep());
        weightFormData.setBar(exercise.getBar());
        weightFormData.setRequiresBar(exercise.isRequiresBar());
        weightFormData.setWeightSpec(exercise.getWeightSpec());

        EditWeightFormDialogFragment dialog = new EditWeightFormDialogFragment(R.string.text_weight,
                result -> {
                    weightEditText.setText(FormatUtils.toString(result.getWeight().getValue()));
                    if (result.isExerciseUpdated()) {
                        exercise.setBar(result.getBar());
                        exercise.setStep(result.getStep());
                        exercise.setWeightSpec(result.getWeightSpec());
                        recyclerViewAdapter.notifyItemRangeChanged(0, log.size());

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

            BigDecimal partialWeight = getWeightFromTotal(
                    bit.getWeight().getValue(usingInternationalSystem),
                    exercise.getWeightSpec(),
                    exercise.getBar(),
                    usingInternationalSystem);

            weight.setText(FormatUtils.toString(partialWeight));
        }
    }

    private void loadMoreHistory() {
        new DBThread(this, db -> {
            final int size = log.size();
            final Bit bit = log.get(size-1);
            final Calendar date = bit.getTimestamp();
            final List<BitEntity> log = db.bitDao().getHistory(exercise.getId(), bit.getTrainingId(),
                    date, LOG_PAGES_SIZE);
            log.stream().map(b -> new Bit().fromEntity(b))
                    .forEach(this.log::add);

            runOnUiThread(() -> recyclerViewAdapter.notifyItemRangeInserted(size, log.size()));
        });
    }

    private void saveBitLog(View view) {
        new DBThread(this, db -> {
            final Bit bit = new Bit();
            bit.setExerciseId(exercise.getId());

            BigDecimal totalWeight = getTotalWeight(
                    toBigDecimal(weight.getText().toString()),
                    exercise.getWeightSpec(),
                    exercise.getBar(),
                    usingInternationalSystem);

            bit.setWeight(new Weight(totalWeight, usingInternationalSystem));
            bit.setNote(notes.getText().toString());
            bit.setReps(toInt(reps.getText().toString()));
            bit.setTimestamp(Calendar.getInstance());
            bit.setTrainingId(trainingId);

            // SAVE TO DB:
            bit.setId((int) db.bitDao().insert(bit.toEntity()));

            runOnUiThread(() -> {
                boolean added = false;
                int idx = 0;
                for (Bit logBit : log) {
                    if (logBit.getTrainingId() == trainingId) {
                        idx++;
                    } else {
                        log.add(idx, bit);
                        recyclerViewAdapter.notifyItemInserted(idx);
                        added = true;
                        break;
                    }
                }

                if (!added) {
                    log.add(bit);
                    recyclerViewAdapter.notifyItemInserted(log.size()-1);
                }
            });
        });
    }

    public void removeBitLog(Bit bit) {
        new DBThread(this, db -> {
            db.bitDao().delete(bit.toEntity());

            int index = log.indexOf(bit);
            int trainingId = bit.getTrainingId();
            log.remove(index);

            runOnUiThread(()-> {
                recyclerViewAdapter.notifyItemRemoved(index);
                if (index == 0) {
                    if (log.get(0).getTrainingId() != trainingId) {
                        recyclerViewAdapter.notifyItemChanged(0);
                    } else {
                        recyclerViewAdapter.notifyTrainingIdChanged(trainingId, 0);
                    }
                } else {
                    recyclerViewAdapter.notifyTrainingIdChanged(trainingId, index);
                }
            });
        });
    }

    private void onClickBit(Bit bit) {
        if (bit == null) {
            loadMoreHistory();
        } else {
            removeBitLog(bit);
        }
    }
}