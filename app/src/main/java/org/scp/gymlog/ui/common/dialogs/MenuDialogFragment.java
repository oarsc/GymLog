package org.scp.gymlog.ui.common.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.scp.gymlog.R;

import java.util.function.Consumer;

public class MenuDialogFragment extends DialogFragment {
    public static final int DIALOG_CLOSED = -1;

    private final int menuId;
    private final Consumer<Integer> callback;
    private boolean callbackCalled = false;

    public MenuDialogFragment(@MenuRes int menuId, Consumer<Integer> callback) {
        this.menuId = menuId;
        this.callback = callback;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_menu, null);
        RecyclerView recyclerView = view.findViewById(R.id.parent_layout);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new MenuRecyclerViewAdapter(getContext(), menuId,
                this::onMenuElementClicked));

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()).setView(view);

        return builder.create();
    }

    private void onMenuElementClicked(int menuItemId) {
        callback.accept(menuItemId);
        callbackCalled = true;
        dismiss();
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        if (!callbackCalled) {
            callback.accept(DIALOG_CLOSED);
        }
        super.onDismiss(dialog);
    }
}
