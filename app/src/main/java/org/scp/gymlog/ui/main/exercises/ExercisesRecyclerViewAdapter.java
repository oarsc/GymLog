package org.scp.gymlog.ui.main.exercises;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.scp.gymlog.databinding.FragmentListElementBinding;
import org.scp.gymlog.model.Data;
import org.scp.gymlog.model.MuscularGroup;

import java.util.List;

public class ExercisesRecyclerViewAdapter extends RecyclerView.Adapter<ExercisesRecyclerViewAdapter.ViewHolder> {

	private final List<MuscularGroup> muscularGroups;

	public ExercisesRecyclerViewAdapter() {
		muscularGroups = Data.getInstance().getGroups();
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new ViewHolder(
				FragmentListElementBinding.inflate(
						LayoutInflater.from(parent.getContext()), parent, false
				)
		);
	}

	@Override
	public void onBindViewHolder(final ViewHolder holder, int position) {
		holder.muscularGroup = muscularGroups.get(position);
		holder.mContentView.setText(holder.muscularGroup.getText());
		holder.mImageView.setImageResource(holder.muscularGroup.getIcon());
	}

	@Override
	public int getItemCount() {
		return muscularGroups.size();
	}

	public class ViewHolder extends RecyclerView.ViewHolder {
		public MuscularGroup muscularGroup;
		public final TextView mContentView;
		public final ImageView mImageView;

		public ViewHolder(FragmentListElementBinding binding) {
			super(binding.getRoot());
			mContentView = binding.content;
			mImageView = binding.image;

			binding.getRoot().setOnClickListener(a->System.out.println("CLICK"));
		}

		@Override
		public String toString() {
			return super.toString() + " '" + mContentView.getText() + "'";
		}
	}
}