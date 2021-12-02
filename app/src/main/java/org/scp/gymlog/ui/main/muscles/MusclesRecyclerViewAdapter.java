package org.scp.gymlog.ui.main.muscles;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.scp.gymlog.databinding.FragmentListElementBinding;
import org.scp.gymlog.model.Data;
import org.scp.gymlog.model.MuscularGroup;
import org.scp.gymlog.ui.exercises.ExercisesActivity;

import java.util.List;

public class MusclesRecyclerViewAdapter extends RecyclerView.Adapter<MusclesRecyclerViewAdapter.ViewHolder> {

	private final List<MuscularGroup> muscularGroups;
	private Context context;

	public MusclesRecyclerViewAdapter() {
		muscularGroups = Data.getInstance().getMuscularGroups();
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		this.context = parent.getContext();
		return new ViewHolder(
				FragmentListElementBinding.inflate(
						LayoutInflater.from(this.context), parent, false
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

			binding.getRoot().setOnClickListener(a-> {
				Intent intent = new Intent(context, ExercisesActivity.class);
				intent.putExtra("muscularGroupId", muscularGroup.getId());
				context.startActivity(intent);
			});
		}

		@Override
		public String toString() {
			return super.toString() + " '" + mContentView.getText() + "'";
		}
	}
}