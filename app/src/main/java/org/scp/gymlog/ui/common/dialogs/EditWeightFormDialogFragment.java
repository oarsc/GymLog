package org.scp.gymlog.ui.common.dialogs;

import static org.scp.gymlog.util.FormatUtils.ONE_THOUSAND;
import static org.scp.gymlog.util.FormatUtils.toBigDecimal;
import static org.scp.gymlog.util.FormatUtils.toKilograms;
import static org.scp.gymlog.util.FormatUtils.toPounds;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.StringRes;

import org.scp.gymlog.R;
import org.scp.gymlog.model.Bar;
import org.scp.gymlog.model.Exercise;
import org.scp.gymlog.model.Weight;
import org.scp.gymlog.model.WeightSpecification;
import org.scp.gymlog.ui.common.dialogs.model.WeightFormData;
import org.scp.gymlog.util.Data;
import org.scp.gymlog.util.FormatUtils;
import org.scp.gymlog.util.Function;

import java.math.BigDecimal;
import java.util.function.Consumer;

public class EditWeightFormDialogFragment extends CustomDialogFragment<WeightFormData> {

    private EditText input;
    private TextView convertValue;
    private TextView convertUnit;
    private TextView unit;
    private TextView barUsed;
    private View incompatibleBar;
    private TextView weightSpec;

    private Exercise exercise;

    public EditWeightFormDialogFragment(@StringRes int title, Consumer<WeightFormData> confirm,
                                        Function cancel, WeightFormData initialValue) {
        super(title, confirm, cancel);
        this.initialValue = initialValue;
        exercise = initialValue.getExercise();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_edit_weight, null);

        setInitialData(view);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(title)
                .setView(view)
                .setPositiveButton(R.string.button_confirm, (dialog, id) ->
                        confirm.accept(confirmData())
                )
                .setNegativeButton(R.string.button_cancel, (dialog, id) -> cancel.call());

        Dialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        input.requestFocus();
        return dialog;
    }

    private void setInitialData(View view) {
        input = view.findViewById(R.id.weightValue);
        convertValue = view.findViewById(R.id.converted);
        convertUnit = view.findViewById(R.id.convert_unit);
        unit = view.findViewById(R.id.unit);
        barUsed = view.findViewById(R.id.bar_used);
        weightSpec = view.findViewById(R.id.weight_spec);
        incompatibleBar = view.findViewById(R.id.incompatible_bar);

        Weight weight = initialValue.getWeight();
        if (weight.getValue().compareTo(BigDecimal.ZERO) != 0) {
            input.setText(FormatUtils.toString(weight.getValue()));
        }
        updateConvertedUnit(weight.getValue());

        if (weight.isInternationalSystem()) {
            convertUnit.setText(R.string.text_lb);
            unit.setText(R.string.text_kg);
        } else {
            convertUnit.setText(R.string.text_kg);
            unit.setText(R.string.text_lb);
        }
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setFilters(new InputFilter[] {(source, start, end, dest, dstart, dend) -> {
            BigDecimal input = FormatUtils.toBigDecimal(dest.toString() + source.toString());
            return input.compareTo(ONE_THOUSAND) < 0 && input.scale() < 3? null : "";
        }});
        input.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {}
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateConvertedUnit(s.toString());
            }
        });

        View layoutBars = view.findViewById(R.id.bars_box);
        layoutBars.setOnClickListener(barUsedView -> {
            PopupMenu popup = new PopupMenu(getActivity(), layoutBars);
            Menu menu = popup.getMenu();

            int order = 0;
            menu.add(0, -1, order++ , "No bar");
            for (Bar bar : Data.getInstance().getBars()) {
                Weight barWeight = bar.getWeight();
                menu.add(0, bar.getId(), order++ , getWeightLabel(barWeight));
            }

            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id < 0) {
                    if (exercise.getBar() != null) {
                        exercise.setBar(null);
                        initialValue.setExerciseUpdated(true);
                        view.findViewById(R.id.incompatible_bar).setVisibility(exercise.isRequiresBar()?
                                View.VISIBLE : View.INVISIBLE);
                        updateSelectedBar();
                    }

                } else {
                    Bar bar = Data.getBar(id);
                    if (exercise.getBar() != bar) {
                        exercise.setBar(bar);
                        initialValue.setExerciseUpdated(true);
                        view.findViewById(R.id.incompatible_bar).setVisibility(exercise.isRequiresBar()?
                                View.INVISIBLE : View.VISIBLE);
                        updateSelectedBar();
                    }
                }
                return true;
            });
            popup.show();
        });
        updateSelectedBar();

        View layoutWeightSpec = view.findViewById(R.id.weights_config_box);
        layoutWeightSpec.setOnClickListener(weightSpecView -> {
            PopupMenu popup = new PopupMenu(getActivity(), layoutWeightSpec);
            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                WeightSpecification newWeightSpec;
                if (id == R.id.total)         newWeightSpec = WeightSpecification.TOTAL_WEIGHT;
                else if (id == R.id.no_bar)   newWeightSpec = WeightSpecification.NO_BAR_WEIGHT;
                else if (id == R.id.one_side) newWeightSpec = WeightSpecification.ONE_SIDE_WEIGHT;
                else return false;

                if (newWeightSpec != exercise.getWeightSpec()) {
                    exercise.setWeightSpec(newWeightSpec);
                    initialValue.setExerciseUpdated(true);
                    updateWeightSpec();
                }
                return true;
            });
            popup.inflate(R.menu.weight_specification_menu);
            popup.show();
        });
        updateWeightSpec();
    }

    private void updateSelectedBar() {
        Bar bar = exercise.getBar();
        if (bar == null) {
            barUsed.setText(R.string.text_none);
            incompatibleBar.setVisibility(exercise.isRequiresBar()? View.VISIBLE : View.INVISIBLE);
        } else {
            barUsed.setText(getWeightLabel(bar.getWeight()));
            incompatibleBar.setVisibility(exercise.isRequiresBar()? View.INVISIBLE : View.VISIBLE);
        }
    }

    private void updateWeightSpec() {
        switch (exercise.getWeightSpec()) {
            case TOTAL_WEIGHT:    weightSpec.setText(R.string.weight_spec_total);    break;
            case NO_BAR_WEIGHT:   weightSpec.setText(R.string.weight_spec_no_bar);   break;
            case ONE_SIDE_WEIGHT: weightSpec.setText(R.string.weight_spec_one_side); break;
        }
    }

    private StringBuilder getWeightLabel(Weight weight) {
        if (initialValue.getWeight().isInternationalSystem())  {
            return new StringBuilder(FormatUtils.toString(weight.toKg()))
                    .append(" kg");
        } else {
            return new StringBuilder(FormatUtils.toString(weight.toLbs()))
                    .append(" lbs");
        }
    }

    private void updateConvertedUnit(String value) {
        updateConvertedUnit(toBigDecimal(value));
    }

    private void updateConvertedUnit(BigDecimal value) {
        BigDecimal convertedValue;
        if (initialValue.getWeight().isInternationalSystem()) {
            convertedValue = toPounds(value);
        } else {
            convertedValue = toKilograms(value);
        }
        convertValue.setText(FormatUtils.toString(convertedValue));
    }

    private WeightFormData confirmData() {
        Weight weight = new Weight(
                FormatUtils.toBigDecimal(input.getText().toString()),
                initialValue.getWeight().isInternationalSystem());
        initialValue.setWeight(weight);
        return initialValue;
    }
}
