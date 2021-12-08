package org.scp.gymlog.ui.common.dialogs;

import static org.scp.gymlog.util.FormatUtils.toBigDecimal;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import org.scp.gymlog.R;
import org.scp.gymlog.ui.common.NumberModifierView;
import org.scp.gymlog.util.FormatUtils;
import org.scp.gymlog.util.Function;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.function.Consumer;

public class EditNumberDialogFragment extends CustomDialogFragment<BigDecimal> {

    private boolean allowNegatives = false;
    private boolean showButtons = true;

    public EditNumberDialogFragment(@StringRes int title, Consumer<BigDecimal> confirm, Function cancel) {
        super(title, confirm, cancel);
        initialValue = BigDecimal.ZERO;
    }

    public EditNumberDialogFragment(@StringRes int title, Consumer<BigDecimal> confirm) {
        this(title, confirm, () -> {});
    }

    public void setAllowNegatives(boolean allowNegatives) {
        this.allowNegatives = allowNegatives;
    }

    public void setInitialValue(String initialValue) {
        setInitialValue(toBigDecimal(initialValue));
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_edit_number, null);
        EditText input = view.findViewById(R.id.dialog_text);
        NumberModifierView modifier = view.findViewById(R.id.modifier);
        input.setText(FormatUtils.toString(initialValue));

        if (allowNegatives) {
            modifier.allowNegatives();
            input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL |
                    InputType.TYPE_NUMBER_FLAG_SIGNED);
        }

        if (!showButtons) {
            modifier.setVisibility(View.INVISIBLE);

            ConstraintLayout constraintLayout = view.findViewById(R.id.parent_layout);
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(constraintLayout);
            Arrays.stream(new int[]{ConstraintSet.RIGHT, ConstraintSet.RIGHT, ConstraintSet.RIGHT, ConstraintSet.RIGHT})
                    .forEach(pos -> constraintSet.connect(R.id.dialog_text, pos, R.id.parent_layout, pos,0));
            constraintSet.applyTo(constraintLayout);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(title)
                .setView(view)
                .setPositiveButton(R.string.button_confirm, (dialog, id) -> {
                    String value = input.getText().toString();
                    confirm.accept(toBigDecimal(value));
                })
                .setNegativeButton(R.string.button_cancel, (dialog, id) -> cancel.call());

        Dialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        input.requestFocus();
        return dialog;
    }
}
