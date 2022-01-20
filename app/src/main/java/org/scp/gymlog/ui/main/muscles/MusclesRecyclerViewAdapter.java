package org.scp.gymlog.ui.main.muscles;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.scp.gymlog.databinding.ListElementFragmentMuscleBinding;
import org.scp.gymlog.model.Muscle;
import org.scp.gymlog.util.Data;

import java.util.List;
import java.util.function.Consumer;

public class MusclesRecyclerViewAdapter extends RecyclerView.Adapter<MusclesRecyclerViewAdapter.ViewHolder> {

	private final List<Muscle> muscles;
	private final Consumer<Muscle> onClickElementListener;

	public MusclesRecyclerViewAdapter(@NonNull Consumer<Muscle> onClickElementListener) {
		this.onClickElementListener = onClickElementListener;
		muscles = Data.getInstance().getMuscles();
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new ViewHolder(
				ListElementFragmentMuscleBinding.inflate(
						LayoutInflater.from(parent.getContext()), parent, false
				)
		);
	}

	@Override
	public void onBindViewHolder(final ViewHolder holder, int position) {
		Context context = holder.itemView.getContext();

		holder.muscle = muscles.get(position);
		holder.mContentView.setText(holder.muscle.getText());

		holder.mImageView.setImageResource(holder.muscle.getIcon());
		holder.mImageView.setImageTintList(ColorStateList.valueOf(
				context.getResources().getColor(holder.muscle.getColor(), null)
		));

		holder.mIndicator.setBackgroundResource(holder.muscle.getColor());
	}

	@Override
	public int getItemCount() {
		return muscles.size();
	}

	public class ViewHolder extends RecyclerView.ViewHolder {
		public Muscle muscle;
		public final TextView mContentView;
		public final ImageView mImageView;
		public final View mIndicator;

		public ViewHolder(ListElementFragmentMuscleBinding binding) {
			super(binding.getRoot());
			mContentView = binding.content;
			mImageView = binding.image;
			mIndicator = binding.indicator;

			itemView.setOnClickListener(a-> onClickElementListener.accept(muscle));
		}

		@Override
		public String toString() {
			return super.toString() + " '" + mContentView.getText() + "'";
		}
	}
}