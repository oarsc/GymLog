package org.scp.gymlog.ui.common.dialogs;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.scp.gymlog.databinding.ListElementFragmentDialogMenuBinding;

import java.util.List;
import java.util.function.BiConsumer;

public class TextSelectRecyclerViewAdapter extends RecyclerView.Adapter<TextSelectRecyclerViewAdapter.ViewHolder> {

    private final List<String> texts;
    private final BiConsumer<Integer, String> onClick;

    public TextSelectRecyclerViewAdapter(List<String> texts, BiConsumer<Integer, String> onClick) {
        this.onClick = onClick;
        this.texts = texts;
    }

    @Override
    public TextSelectRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new TextSelectRecyclerViewAdapter.ViewHolder(
                ListElementFragmentDialogMenuBinding.inflate(
                        LayoutInflater.from(parent.getContext()), parent, false
                )
        );
    }

    @Override
    @SuppressLint("RecyclerView")
    public void onBindViewHolder(final TextSelectRecyclerViewAdapter.ViewHolder holder, int position) {
        holder.mContentView.setText(texts.get(position));
        holder.mImageView.setVisibility(View.GONE);
        holder.index = position;
    }

    @Override
    public int getItemCount() {
        return this.texts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView mContentView;
        public final ImageView mImageView;
        public int index;

        public ViewHolder(ListElementFragmentDialogMenuBinding binding) {
            super(binding.getRoot());
            mContentView = binding.content;
            mImageView = binding.image;
            itemView.setOnClickListener(view -> onClick.accept(index, texts.get(index)));
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}