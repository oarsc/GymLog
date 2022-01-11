package org.scp.gymlog.ui.common.dialogs;

import static org.scp.gymlog.util.Constants.ONE_HUNDRED;
import static org.scp.gymlog.util.FormatUtils.toKilograms;
import static org.scp.gymlog.util.FormatUtils.toPounds;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.StringRes;

import org.scp.gymlog.R;
import org.scp.gymlog.exceptions.LoadException;
import org.scp.gymlog.model.Bar;
import org.scp.gymlog.model.Exercise;
import org.scp.gymlog.model.Weight;
import org.scp.gymlog.model.WeightSpecification;
import org.scp.gymlog.room.DBThread;
import org.scp.gymlog.room.entities.ExerciseEntity;
import org.scp.gymlog.util.Data;
import org.scp.gymlog.util.FormatUtils;
import org.scp.gymlog.util.Function;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class EditExercisesLastsDialogFragment extends CustomDialogFragment<Exercise> {

    private TextView barUsed;
    private View incompatibleBar;
    private TextView weightSpecView;
    private ImageView weightSpecIcon;
    private TextView stepView;


    private boolean exerciseUpdate;
    private BigDecimal step;
    private Bar bar;
    private WeightSpecification weightSpec;

    private boolean internationalSystem;



    public EditExercisesLastsDialogFragment(@StringRes int title, Consumer<Exercise> confirm,
                                            Function cancel, Exercise initialValue, boolean internationalSystem) {
        super(title, confirm, cancel);
        this.initialValue = initialValue;
        this.internationalSystem = internationalSystem;

        bar = initialValue.getBar();
        step = initialValue.getStep();
        weightSpec = initialValue.getWeightSpec();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_edit_exercise_lasts, null);

        setInitialData(view);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(title)
                .setView(view)
                .setPositiveButton(R.string.button_confirm, (dialog, id) -> confirm())
                .setNegativeButton(R.string.button_cancel, (dialog, id) -> cancel.call());
        return builder.create();
    }

    private void setInitialData(View view) {
        barUsed = view.findViewById(R.id.barUsed);
        weightSpecView = view.findViewById(R.id.weightSpec);
        weightSpecIcon = view.findViewById(R.id.weightSpecIcon);
        incompatibleBar = view.findViewById(R.id.incompatibleBar);
        stepView = view.findViewById(R.id.step);

        View layoutStep = view.findViewById(R.id.stepBox);
        layoutStep.setOnClickListener(barUsedView -> {
            PopupMenu popup = new PopupMenu(getActivity(), layoutStep);
            Menu menu = popup.getMenu();

            Arrays.stream(Data.STEPS_KG).forEach(size->
                menu.add(0, size, size , FormatUtils.toString(BigDecimal.valueOf(size).divide(ONE_HUNDRED)))
            );

            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                BigDecimal newStep = BigDecimal.valueOf(id).divide(ONE_HUNDRED);
                if (newStep.compareTo(step) != 0) {
                    step = newStep;
                    exerciseUpdate = true;
                    updateStep();
                }
                return true;
            });
            popup.show();
        });
        updateStep();

        View layoutBars = view.findViewById(R.id.barsBox);
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

                    if (bar != null) {
                        bar = null;
                        exerciseUpdate = true;
                        if (initialValue.isRequiresBar()) {
                            view.findViewById(R.id.incompatibleBar).setVisibility(View.VISIBLE);
                            Toast.makeText(getContext(), R.string.validation_should_have_bar,
                                    Toast.LENGTH_LONG).show();
                        } else {
                            view.findViewById(R.id.incompatibleBar).setVisibility(View.INVISIBLE);
                        }

                        if (weightSpec == WeightSpecification.TOTAL_WEIGHT) {
                            weightSpec = WeightSpecification.NO_BAR_WEIGHT;
                            updateWeightSpec();
                        }

                        updateSelectedBar();
                    }

                } else {
                    Bar bar = Data.getBar(id);
                    if (this.bar != bar) {
                        this.bar = bar;
                        exerciseUpdate = true;
                        if (initialValue.isRequiresBar()) {
                            view.findViewById(R.id.incompatibleBar).setVisibility(View.INVISIBLE);
                        } else {
                            view.findViewById(R.id.incompatibleBar).setVisibility(View.VISIBLE);
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

        View layoutWeightSpec = view.findViewById(R.id.weightsConfigBox);
        layoutWeightSpec.setOnClickListener(weightSpecView -> {
            PopupMenu popup = new PopupMenu(getActivity(), layoutWeightSpec);
            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                WeightSpecification newWeightSpec;
                if (id == R.id.total)        newWeightSpec = WeightSpecification.TOTAL_WEIGHT;
                else if (id == R.id.noBar)   newWeightSpec = WeightSpecification.NO_BAR_WEIGHT;
                else if (id == R.id.oneSide) newWeightSpec = WeightSpecification.ONE_SIDE_WEIGHT;
                else return false;

                if (newWeightSpec != weightSpec) {
                    weightSpec = newWeightSpec;
                    exerciseUpdate = true;
                    updateWeightSpec();
                }
                return true;
            });
            popup.inflate(R.menu.weight_specification_menu);

            if (bar == null) {
                popup.getMenu().findItem(R.id.total).setVisible(false);
            }

            popup.show();
        });
        updateWeightSpec();
    }

    private void updateStep() {
        stepView.setText(FormatUtils.toString(step));
    }

    private void updateSelectedBar() {
        if (bar == null) {
            barUsed.setText(R.string.symbol_hyphen);
            incompatibleBar.setVisibility(initialValue.isRequiresBar()? View.VISIBLE : View.INVISIBLE);
        } else {
            barUsed.setText(getWeightLabel(bar.getWeight()));
            incompatibleBar.setVisibility(initialValue.isRequiresBar()? View.INVISIBLE : View.VISIBLE);
        }
    }

    private void updateWeightSpec() {
        weightSpecView.setText(weightSpec.literal);
        weightSpecIcon.setImageResource(weightSpec.icon);
    }

    private StringBuilder getWeightLabel(Weight weight) {
        if (internationalSystem)  {
            return new StringBuilder(FormatUtils.toString(weight.toKg()))
                    .append(" kg");
        } else {
            return new StringBuilder(FormatUtils.toString(weight.toLbs()))
                    .append(" lbs");
        }
    }

    private void confirm() {
        if (exerciseUpdate) {
            DBThread.run(getContext(), db -> {

                ExerciseEntity exercises = db.exerciseDao().getById(initialValue.getId())
                        .orElseThrow(() -> new LoadException("Didn't find exercise with id: "+
                                initialValue.getId()));

                exercises.lastBarId = bar == null? null : bar.getId();
                exercises.lastWeightSpec = weightSpec;
                exercises.lastStep = step.multiply(ONE_HUNDRED).intValue();

                db.exerciseDao().update(exercises);

                initialValue.setBar(bar);
                initialValue.setWeightSpec(weightSpec);
                initialValue.setStep(step);
                confirm.accept(initialValue);
            });
        } else {
            confirm.accept(null);
        }
    }
}
