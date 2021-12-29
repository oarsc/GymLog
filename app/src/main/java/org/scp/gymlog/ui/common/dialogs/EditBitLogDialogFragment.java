package org.scp.gymlog.ui.common.dialogs;

import static org.scp.gymlog.util.FormatUtils.toBigDecimal;
import static org.scp.gymlog.util.FormatUtils.toInt;
import static org.scp.gymlog.util.WeightUtils.getTotalWeight;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.StringRes;

import com.google.android.material.switchmaterial.SwitchMaterial;

import org.scp.gymlog.R;
import org.scp.gymlog.model.Bit;
import org.scp.gymlog.model.Exercise;
import org.scp.gymlog.model.Weight;
import org.scp.gymlog.ui.common.components.NumberModifierView;
import org.scp.gymlog.util.FormatUtils;
import org.scp.gymlog.util.Function;
import org.scp.gymlog.util.WeightUtils;

import java.math.BigDecimal;
import java.util.function.Consumer;

public class EditBitLogDialogFragment extends CustomDialogFragment<Bit> {

    private final Exercise exercise;
    private final boolean internationalSystem;
    private final boolean enableInstantSwitch;

    public EditBitLogDialogFragment(@StringRes int title, Exercise exercise,
                                    boolean enableInstantSwitch,
                                    boolean internationalSystem, Consumer<Bit> confirm,
                                    Function cancel) {
        super(title, confirm, cancel);
        this.enableInstantSwitch = enableInstantSwitch;
        this.exercise = exercise;
        this.internationalSystem = internationalSystem;
    }

    public EditBitLogDialogFragment(@StringRes int title, Exercise exercise,
                                    boolean enableInstantSwitch,
                                    boolean internationalSystem, Consumer<Bit> confirm) {
        this(title, exercise, enableInstantSwitch, internationalSystem, confirm, () -> {});
    }

    public void setInitialValue(Bit bit) {
        this.initialValue = bit;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_edit_bit_log, null);

        EditText editNotes = view.findViewById(R.id.editNotes);
        editNotes.setText(initialValue.getNote());

        editNotes.setOnClickListener(v -> {
            EditNotesDialogFragment dialog = new EditNotesDialogFragment(R.string.text_notes,
                    exercise.getId(),
                    result -> editNotes.setText(result));
            dialog.setInitialValue(editNotes.getText().toString());
            dialog.show(getChildFragmentManager(), null);
        });

        view.findViewById(R.id.clearButton).setOnClickListener(btn -> editNotes.getText().clear());

        BigDecimal weight = WeightUtils.getWeightFromTotal(
                initialValue.getWeight().getValue(internationalSystem),
                exercise.getWeightSpec(),
                exercise.getBar(),
                internationalSystem
        );

        EditText editWeight = view.findViewById(R.id.editWeight);
        NumberModifierView modifier = view.findViewById(R.id.weightModifier);
        editWeight.setText(FormatUtils.toString(weight));
        modifier.setStep(exercise.getStep());

        EditText editReps = view.findViewById(R.id.editReps);
        editReps.setText(String.valueOf(initialValue.getReps()));

        SwitchMaterial instantSwitch = view.findViewById(R.id.instantSwitch);
        if (enableInstantSwitch) {
            instantSwitch.setChecked(initialValue.isInstant());
        } else {
            instantSwitch.setEnabled(false);
            instantSwitch.setChecked(false);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(title)
                .setView(view)
                .setPositiveButton(R.string.button_confirm, (dialog, id) -> {
                    BigDecimal totalWeight = getTotalWeight(
                            toBigDecimal(editWeight.getText().toString()),
                            exercise.getWeightSpec(),
                            exercise.getBar(),
                            internationalSystem);

                    initialValue.setWeight(new Weight(totalWeight, internationalSystem));
                    initialValue.setReps(toInt(editReps.getText().toString()));
                    initialValue.setNote(editNotes.getText().toString());
                    initialValue.setInstant(instantSwitch.isChecked());
                    confirm.accept(initialValue);
                })
                .setNegativeButton(R.string.button_cancel, (dialog, id) -> cancel.call());

        return builder.create();
    }
}
