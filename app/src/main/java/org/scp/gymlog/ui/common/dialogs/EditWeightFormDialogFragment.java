package org.scp.gymlog.ui.common.dialogs;

import static org.scp.gymlog.util.Constants.ONE_HUNDRED;
import static org.scp.gymlog.util.Constants.ONE_THOUSAND;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.StringRes;

import org.scp.gymlog.R;
import org.scp.gymlog.model.Bar;
import org.scp.gymlog.model.Exercise;
import org.scp.gymlog.model.Weight;
import org.scp.gymlog.model.WeightSpecification;
import org.scp.gymlog.ui.common.NumberModifierView;
import org.scp.gymlog.ui.common.dialogs.model.WeightFormData;
import org.scp.gymlog.util.Data;
import org.scp.gymlog.util.FormatUtils;
import org.scp.gymlog.util.Function;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.function.Consumer;

public class EditWeightFormDialogFragment extends CustomDialogFragment<WeightFormData> {

    private EditText input;
    private TextView convertValue;
    private TextView totalValue;
    private TextView barUsed;
    private View incompatibleBar;
    private TextView weightSpec;
    private ImageView weightSpecIcon;
    private NumberModifierView modifier;
    private TextView step;

    private Exercise exercise;
    private Weight weight;

    public EditWeightFormDialogFragment(@StringRes int title, Consumer<WeightFormData> confirm,
                                        Function cancel, WeightFormData initialValue) {
        super(title, confirm, cancel);
        this.initialValue = initialValue;
        exercise = initialValue.getExercise();
        weight = initialValue.getWeight();
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
        //dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        //input.requestFocus();
        return dialog;
    }

    private void setInitialData(View view) {
        input = view.findViewById(R.id.weightValue);
        convertValue = view.findViewById(R.id.converted);
        totalValue = view.findViewById(R.id.total_weight);
        barUsed = view.findViewById(R.id.bar_used);
        weightSpec = view.findViewById(R.id.weight_spec);
        weightSpecIcon = view.findViewById(R.id.weight_spec_icon);
        incompatibleBar = view.findViewById(R.id.incompatible_bar);
        step = view.findViewById(R.id.step);
        modifier = view.findViewById(R.id.modifier);

        TextView convertUnit = view.findViewById(R.id.convert_unit);
        TextView[] unit = new TextView[] {
                view.findViewById(R.id.unit),
                view.findViewById(R.id.total_unit),
        };

        if (weight.getValue().compareTo(BigDecimal.ZERO) != 0) {
            input.setText(FormatUtils.toString(weight.getValue()));
        }
        updateConvertedUnit(weight.getValue());

        if (weight.isInternationalSystem()) {
            convertUnit.setText(R.string.text_lb);
            Arrays.stream(unit).forEach(x -> x.setText(R.string.text_kg));
        } else {
            convertUnit.setText(R.string.text_kg);
            Arrays.stream(unit).forEach(x -> x.setText(R.string.text_lb));
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
                BigDecimal newWeight = toBigDecimal(s.toString());
                updateConvertedUnit(newWeight);
                updateTotalWeight(newWeight);
            }
        });

        View layoutStep = view.findViewById(R.id.step_box);
        layoutStep.setOnClickListener(barUsedView -> {
            PopupMenu popup = new PopupMenu(getActivity(), layoutStep);
            Menu menu = popup.getMenu();

            Arrays.asList(50, 100, 125, 250, 500, 1000, 1500, 2000).forEach(size->
                menu.add(0, size, size , FormatUtils.toString(BigDecimal.valueOf(size).divide(ONE_HUNDRED)))
            );

            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                BigDecimal newStep = BigDecimal.valueOf(id).divide(ONE_HUNDRED);
                if (newStep.compareTo(exercise.getStep()) != 0) {
                    exercise.setStep(newStep);
                    initialValue.setExerciseUpdated(true);
                    updateStep();
                }
                return true;
            });
            popup.show();
        });
        updateStep();

        View layoutBars = view.findViewById(R.id.bars_box);
        layoutBars.setOnClickListener(barUsedView -> {
            PopupMenu popup = new PopupMenu(getActivity(), layoutBars);
            Menu menu = popup.getMenu();

            int order = 0;
            menu.add(0, -1, order++ , R.string.text_no_bar);
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
                        if (exercise.isRequiresBar()) {
                            view.findViewById(R.id.incompatible_bar).setVisibility(View.VISIBLE);
                            Toast.makeText(getContext(), R.string.validation_should_have_bar,
                                    Toast.LENGTH_LONG).show();
                        } else {
                            view.findViewById(R.id.incompatible_bar).setVisibility(View.INVISIBLE);
                        }
                        updateSelectedBar();
                    }

                } else {
                    Bar bar = Data.getBar(id);
                    if (exercise.getBar() != bar) {
                        exercise.setBar(bar);
                        initialValue.setExerciseUpdated(true);
                        if (exercise.isRequiresBar()) {
                            view.findViewById(R.id.incompatible_bar).setVisibility(View.INVISIBLE);
                        } else {
                            view.findViewById(R.id.incompatible_bar).setVisibility(View.VISIBLE);
                            Toast.makeText(getContext(), R.string.validation_shouldnt_have_bar,
                                    Toast.LENGTH_LONG).show();
                        }
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

    private void updateStep() {
        modifier.setStep(exercise.getStep());
        step.setText(FormatUtils.toString(exercise.getStep()));
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
        updateTotalWeight(null);
    }

    private void updateWeightSpec() {
        WeightSpecification weightSpecification = exercise.getWeightSpec();
        weightSpec.setText(weightSpecification.literal);
        weightSpecIcon.setImageResource(weightSpecification.icon);
        updateTotalWeight(null);
    }

    private StringBuilder getWeightLabel(Weight weight) {
        if (this.weight.isInternationalSystem())  {
            return new StringBuilder(FormatUtils.toString(weight.toKg()))
                    .append(" kg");
        } else {
            return new StringBuilder(FormatUtils.toString(weight.toLbs()))
                    .append(" lbs");
        }
    }

    private void updateConvertedUnit(BigDecimal value) {
        BigDecimal convertedValue;
        if (weight.isInternationalSystem()) {
            convertedValue = toPounds(value);
        } else {
            convertedValue = toKilograms(value);
        }
        convertValue.setText(FormatUtils.toString(convertedValue));
    }

    private void updateTotalWeight(BigDecimal value) {
        if (value == null)
            value = toBigDecimal(input.getText().toString());

        Bar bar = exercise.getBar();
        boolean internationalSystem = weight.isInternationalSystem();

        switch (exercise.getWeightSpec()) {
            case TOTAL_WEIGHT:
                totalValue.setText(FormatUtils.toString(value));
                break;
            case NO_BAR_WEIGHT:
                BigDecimal totalNoBarVal = bar == null? value :
                        value.add(bar.getWeight().getValue(internationalSystem));
                totalValue.setText(FormatUtils.toString(totalNoBarVal));
                break;
            case ONE_SIDE_WEIGHT:
                BigDecimal totalOneSideVal = bar == null? value.add(value) :
                        value.add(value).add(bar.getWeight().getValue(internationalSystem));
                totalValue.setText(FormatUtils.toString(totalOneSideVal));
                break;
        }
    }

    private WeightFormData confirmData() {
        weight = new Weight(
                FormatUtils.toBigDecimal(input.getText().toString()),
                weight.isInternationalSystem());
        initialValue.setWeight(weight);
        return initialValue;
    }
}
