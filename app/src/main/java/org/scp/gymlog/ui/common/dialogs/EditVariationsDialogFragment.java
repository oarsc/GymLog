package org.scp.gymlog.ui.common.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.scp.gymlog.R;
import org.scp.gymlog.model.Variation;
import org.scp.gymlog.util.LambdaUtils;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class EditVariationsDialogFragment extends DialogFragment {

    private final List<Variation> variations;
    private final Consumer<List<Variation>> confirm;
    private EditText input;
    private int selectedIndex;
    private EditVariationsRecyclerViewAdapter adapter;

    public EditVariationsDialogFragment(List<Variation> variations, Consumer<List<Variation>> confirm) {
        this.confirm = confirm;
        this.variations = variations.stream().map(Variation::clone).collect(Collectors.toList());
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = getContext();
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_edit_variations, null);

        input = view.findViewById(R.id.dialogText);
        input.setEnabled(false);
        input.setOnFocusChangeListener((v, hasFocus) ->
                input.post(() -> {
                    InputMethodManager inputMethodManager = (InputMethodManager) getContext()
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
                })
            );
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                adapter.updateText(selectedIndex, s.toString());
            }
        });

        RecyclerView recyclerView = view.findViewById(R.id.variationsList);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter = new EditVariationsRecyclerViewAdapter(variations,
                this::selectVariation));

        ImageView addButton = view.findViewById(R.id.addButton);
        addButton.setOnClickListener(v -> {
            Variation variation = new Variation();
            variation.setName("New "+variations.size());
            variations.add(variation);
            adapter.notifyItemInserted(variations.size()-1);
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.text_variations)
                .setView(view)
                .setPositiveButton(R.string.button_confirm, (dialog, id) -> confirm.accept(variations))
                .setNegativeButton(R.string.button_cancel, (dialog, id) -> {});

        return builder.create();
    }

    private void selectVariation(int index, String name) {
        selectedIndex = index;
        input.setEnabled(true);
        input.setText(name);
        input.setSelection(name.length());
        input.requestFocus();
    }
}
