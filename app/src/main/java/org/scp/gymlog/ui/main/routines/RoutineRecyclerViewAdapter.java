package org.scp.gymlog.ui.main.routines;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.scp.gymlog.databinding.ListElementFragmentBinding;
import org.scp.gymlog.ui.main.routines.placeholder.PlaceholderContent;
import org.scp.gymlog.ui.main.routines.placeholder.PlaceholderContent.PlaceholderItem;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link PlaceholderItem}.
 * TODO: Replace the implementation with code for your data type.
 */
public class RoutineRecyclerViewAdapter extends RecyclerView.Adapter<RoutineRecyclerViewAdapter.ViewHolder> {

	private final List<PlaceholderItem> mValues;

	public RoutineRecyclerViewAdapter() {
		mValues = PlaceholderContent.ITEMS;
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new ViewHolder(
				ListElementFragmentBinding.inflate(
						LayoutInflater.from(parent.getContext()), parent, false
				)
		);
	}

	@Override
	public void onBindViewHolder(final ViewHolder holder, int position) {
		holder.mItem = mValues.get(position);
		holder.mContentView.setText(mValues.get(position).content);
	}

	@Override
	public int getItemCount() {
		return mValues.size();
	}

	public class ViewHolder extends RecyclerView.ViewHolder {
		public final TextView mContentView;
		public PlaceholderItem mItem;

		public ViewHolder(ListElementFragmentBinding binding) {
			super(binding.getRoot());
			mContentView = binding.content;
		}

		@Override
		public String toString() {
			return super.toString() + " '" + mContentView.getText() + "'";
		}
	}
}