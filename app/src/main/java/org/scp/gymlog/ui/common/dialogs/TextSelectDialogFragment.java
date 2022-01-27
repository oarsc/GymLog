package org.scp.gymlog.ui.common.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.scp.gymlog.R;

import java.util.List;
import java.util.function.BiConsumer;

public class TextSelectDialogFragment extends DialogFragment {
    public static final int DIALOG_CLOSED = -1;

    private final List<String> texts;
    private final BiConsumer<Integer, String> callback;
    private boolean callbackCalled = false;

    public TextSelectDialogFragment(List<String> texts, BiConsumer<Integer, String> callback) {
        this.texts = texts;
        this.callback = callback;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_menu, null);
        RecyclerView recyclerView = view.findViewById(R.id.parentLayout);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new TextSelectRecyclerViewAdapter(texts,
                this::onMenuElementClicked));

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()).setView(view);

        return builder.create();
    }

    private void onMenuElementClicked(int index, String text) {
        callback.accept(index, text);
        callbackCalled = true;
        dismiss();
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        if (!callbackCalled) {
            callback.accept(DIALOG_CLOSED, null);
        }
        super.onDismiss(dialog);
    }
}
