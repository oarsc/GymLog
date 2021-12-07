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

import androidx.annotation.StringRes;

import org.scp.gymlog.R;
import org.scp.gymlog.util.Function;

import java.math.BigDecimal;
import java.util.function.Consumer;

public class EditNumberDialogFragment extends CustomDialogFragment<BigDecimal> {

    private boolean allowNegatives = false;

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

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_edit_text, null);
        EditText input = view.findViewById(R.id.dialog_text);
        input.setText(initialValue.toString());

        if (allowNegatives) {
            input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL |
                    InputType.TYPE_NUMBER_FLAG_SIGNED);
        } else {
            input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
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
