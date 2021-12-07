package org.scp.gymlog.ui.common.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import androidx.annotation.StringRes;
import androidx.fragment.app.DialogFragment;

import org.scp.gymlog.R;
import org.scp.gymlog.util.Function;

import java.math.BigDecimal;
import java.util.function.Consumer;

public class EditTextDialogFragment extends CustomDialogFragment<String> {

    public EditTextDialogFragment(@StringRes int title, Consumer<String> confirm, Function cancel) {
        super(title, confirm, cancel);
        initialValue = "";
    }

    public EditTextDialogFragment(@StringRes int title, Consumer<String> confirm) {
        this(title, confirm, () -> {});
    }

    public void setInitialValue(String initialValue) {
        this.initialValue = initialValue;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_edit_text, null);
        EditText input = view.findViewById(R.id.dialog_text);
        input.setText(initialValue);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(title)
                .setView(view)
                .setPositiveButton(R.string.button_confirm, (dialog, id) -> {
                    String text = input.getText().toString();
                    confirm.accept(text);
                })
                .setNegativeButton(R.string.button_cancel, (dialog, id) -> cancel.call());

        Dialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        input.requestFocus();
        return dialog;
    }
}
