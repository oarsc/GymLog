package org.scp.gymlog.ui.exercises;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import org.scp.gymlog.databinding.FragmentExerciseGroupElementBinding;
import org.scp.gymlog.model.Data;
import org.scp.gymlog.model.MuscularGroup;

import java.util.List;

public class ExerciseGroupRecyclerViewAdapter extends RecyclerView.Adapter<ExerciseGroupRecyclerViewAdapter.ViewHolder> {

	private final List<MuscularGroup> muscularGroups;

	public ExerciseGroupRecyclerViewAdapter() {
		muscularGroups = Data.getInstance().getGroups();
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new ViewHolder(
				FragmentExerciseGroupElementBinding.inflate(
						LayoutInflater.from(parent.getContext()), parent, false
				)
		);
	}

	@Override
	public void onBindViewHolder(final ViewHolder holder, int position) {
		holder.muscularGroup = muscularGroups.get(position);
		holder.mContentView.setText(holder.muscularGroup.getText());
	}

	@Override
	public int getItemCount() {
		return muscularGroups.size();
	}

	public class ViewHolder extends RecyclerView.ViewHolder {
		public MuscularGroup muscularGroup;
		public final TextView mContentView;

		public ViewHolder(FragmentExerciseGroupElementBinding binding) {
			super(binding.getRoot());
			mContentView = binding.content;

			binding.getRoot().setOnClickListener(a->System.out.println("CLICK"));
		}

		@Override
		public String toString() {
			return super.toString() + " '" + mContentView.getText() + "'";
		}
	}
}