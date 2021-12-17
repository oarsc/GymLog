package org.scp.gymlog.ui.common.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.fragment.app.DialogFragment;

import org.scp.gymlog.R;

import java.util.function.Consumer;

public class TextDialogFragment extends DialogFragment {

    private final int title;
    private final int text;
    private final Consumer<Boolean> callback;

    public TextDialogFragment(@StringRes int title, @StringRes int text, Consumer<Boolean> callback) {
        this.title = title;
        this.text = text;
        this.callback = callback;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_show_text, null);
        TextView textView = view.findViewById(R.id.text);
        textView.setText(text);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(title)
                .setView(view)
                .setPositiveButton(R.string.button_confirm, (dialog, id) -> callback.accept(true))
                .setNegativeButton(R.string.button_cancel, (dialog, id) -> callback.accept(false));

        return builder.create();
    }
}
