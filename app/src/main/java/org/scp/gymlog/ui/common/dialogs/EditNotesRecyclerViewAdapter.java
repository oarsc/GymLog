package org.scp.gymlog.ui.common.dialogs;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.scp.gymlog.databinding.ListElementFragmentTextBinding;

import java.util.List;
import java.util.function.Consumer;

public class EditNotesRecyclerViewAdapter extends RecyclerView.Adapter<EditNotesRecyclerViewAdapter.ViewHolder> {

    private final List<String> values;
    private final Consumer<String> onClick;

    public EditNotesRecyclerViewAdapter(List<String> values, Consumer<String> onClick) {
        this.values = values;
        this.onClick = onClick;
    }

    @Override
    public EditNotesRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new EditNotesRecyclerViewAdapter.ViewHolder(
                ListElementFragmentTextBinding.inflate(
                        LayoutInflater.from(parent.getContext()), parent, false
                )
        );
    }

    @Override
    public void onBindViewHolder(final EditNotesRecyclerViewAdapter.ViewHolder holder, int position) {
        holder.mContentView.setText(values.get(position));
    }

    @Override
    public int getItemCount() {
        return values.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView mContentView;

        public ViewHolder(ListElementFragmentTextBinding binding) {
            super(binding.getRoot());
            mContentView = binding.content;

            itemView.setOnClickListener(view ->
                onClick.accept(mContentView.getText().toString())
            );
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}