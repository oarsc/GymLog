package org.scp.gymlog.ui.common.dialogs;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.MenuRes;
import androidx.recyclerview.widget.RecyclerView;

import org.scp.gymlog.databinding.ListElementFragmentDialogMenuBinding;

import java.util.function.Consumer;

public class MenuRecyclerViewAdapter extends RecyclerView.Adapter<MenuRecyclerViewAdapter.ViewHolder> {

    private final Menu menu;
    private final Consumer<Integer> onClick;

    public MenuRecyclerViewAdapter(Context context, @MenuRes int menuId, Consumer<Integer> onClick) {
        this.onClick = onClick;

        PopupMenu popupMenu = new PopupMenu(context, null);
        popupMenu.inflate(menuId);
        menu = popupMenu.getMenu();
    }

    @Override
    public MenuRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MenuRecyclerViewAdapter.ViewHolder(
                ListElementFragmentDialogMenuBinding.inflate(
                        LayoutInflater.from(parent.getContext()), parent, false
                )
        );
    }

    @Override
    public void onBindViewHolder(final MenuRecyclerViewAdapter.ViewHolder holder, int position) {
        MenuItem element = menu.getItem(position);
        holder.mContentView.setText(element.getTitle());
        holder.mImageView.setImageDrawable(element.getIcon());
        holder.id = element.getItemId();
    }

    @Override
    public int getItemCount() {
        return this.menu.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView mContentView;
        public final ImageView mImageView;
        public int id;

        public ViewHolder(ListElementFragmentDialogMenuBinding binding) {
            super(binding.getRoot());
            mContentView = binding.content;
            mImageView = binding.image;
            binding.getRoot().setOnClickListener(view -> onClick.accept(id));
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}