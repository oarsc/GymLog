package org.scp.gymlog.ui.common.dialogs;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.scp.gymlog.databinding.ListElementFragmentTextBinding;
import org.scp.gymlog.model.Variation;

import java.util.List;
import java.util.function.BiConsumer;

public class EditVariationsRecyclerViewAdapter extends RecyclerView.Adapter<EditVariationsRecyclerViewAdapter.ViewHolder> {

    private final List<Variation> variations;
    private final BiConsumer<Integer, String> onClick;

    public EditVariationsRecyclerViewAdapter(List<Variation> variations, BiConsumer<Integer, String> onClick) {
        this.variations = variations;
        this.onClick = onClick;
    }

    @Override
    public EditVariationsRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new EditVariationsRecyclerViewAdapter.ViewHolder(
                ListElementFragmentTextBinding.inflate(
                        LayoutInflater.from(parent.getContext()), parent, false
                )
        );
    }

    @Override
    public void onBindViewHolder(final EditVariationsRecyclerViewAdapter.ViewHolder holder, int position) {
        holder.variation = variations.get(position);
        holder.mContentView.setText(holder.variation.getName());
    }

    @Override
    public int getItemCount() {
        return variations.size();
    }

    public void updateText(int index, String text) {
        variations.get(index).setName(text);
        notifyItemChanged(index);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView mContentView;
        public Variation variation;

        public ViewHolder(ListElementFragmentTextBinding binding) {
            super(binding.getRoot());
            mContentView = binding.content;

            itemView.setOnClickListener(view -> {
                String name = mContentView.getText().toString();
                int index = variations.indexOf(variation);
                onClick.accept(index, name);
            });
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}