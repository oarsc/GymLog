package org.scp.gymlog.ui.common.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.StringRes;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.scp.gymlog.R;
import org.scp.gymlog.room.DBThread;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class EditNotesDialogFragment extends CustomDialogFragment<String> {

    private final int exerciseId;

    public EditNotesDialogFragment(@StringRes int title, int exerciseId, Consumer<String> confirm) {
        super(title, confirm, null);
        initialValue = "";
        this.exerciseId = exerciseId;
    }

    public void setInitialValue(String initialValue) {
        this.initialValue = initialValue;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = getContext();
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_edit_notes, null);
        EditText input = view.findViewById(R.id.dialog_text);
        input.setText(initialValue);

        new DBThread(context, db -> {
            List<String> notes = db.bitDao().getNotesHistory(exerciseId,18);

            RecyclerView recyclerView = view.findViewById(R.id.historic_notes);
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setAdapter(new EditNotesRecyclerViewAdapter(notes, input::setText));
        });

        ImageView clearButton = view.findViewById(R.id.clear_button);
        clearButton.setOnClickListener(v -> input.getText().clear());


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(title)
                .setView(view)
                .setPositiveButton(R.string.button_confirm, (dialog, id) -> {
                    String text = input.getText().toString();
                    confirm.accept(text);
                })
                .setNegativeButton(R.string.button_cancel, (dialog, id) -> {});

        return builder.create();
    }
}
