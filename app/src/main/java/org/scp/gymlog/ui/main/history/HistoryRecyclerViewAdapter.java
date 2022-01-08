package org.scp.gymlog.ui.main.history;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.scp.gymlog.R;
import org.scp.gymlog.databinding.ListElementFragmentTrainingBinding;
import org.scp.gymlog.model.Muscle;
import org.scp.gymlog.ui.training.TrainingActivity;
import org.scp.gymlog.util.DateUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HistoryRecyclerViewAdapter extends RecyclerView.Adapter<HistoryRecyclerViewAdapter.ViewHolder> {

	private List<TrainingData> trainingDataList;
	private Context context;

	public HistoryRecyclerViewAdapter() {
		trainingDataList = new ArrayList<>();
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		this.context = parent.getContext();
		return new ViewHolder(
				ListElementFragmentTrainingBinding.inflate(
						LayoutInflater.from(this.context), parent, false
				)
		);
	}

	@SuppressLint("SetTextI18n")
	@Override
	public void onBindViewHolder(final ViewHolder holder, int position) {
		TrainingData data = trainingDataList.get(position);;
		holder.id = data.getId();
		holder.mTitle.setText(context.getResources().getString(R.string.text_training)
				+" #" + data.getId() + ": "+ context.getResources().getString(R.string.text_started_at)
				+" " + DateUtils.getTime(data.getStartDate()));
		holder.mSubtitle.setText(
				data.getMostUsedMuscles().stream()
						.map(Muscle::getText)
						.map(context.getResources()::getString)
						.collect(Collectors.joining(", ")));
		holder.mIndicator.setBackgroundResource(data.getMostUsedMuscles().get(0).getColor());
	}

	public void clear() {
		trainingDataList.clear();
	}

	public int size() {
		return trainingDataList.size();
	}

	public void add(TrainingData trainingData) {
		trainingDataList.add(trainingData);
	}

	@SuppressLint("NotifyDataSetChanged")
	public void notifyItemsChanged(int initialSize, int endSize) {
		if (initialSize == 0) {
			if (endSize != 0) {
				notifyItemInserted(endSize);
			}
		} else if (endSize == 0) {
			notifyItemRangeRemoved(0, initialSize);

		} else if (initialSize == endSize) {
			notifyItemRangeChanged(0, initialSize);

		} else {
			notifyDataSetChanged();
		}
	}

	@Override
	public int getItemCount() {
		return trainingDataList.size();
	}

	public class ViewHolder extends RecyclerView.ViewHolder {
		public int id;
		public final TextView mTitle, mSubtitle;
		public final View mIndicator;

		public ViewHolder(ListElementFragmentTrainingBinding binding) {
			super(binding.getRoot());
			mTitle = binding.title;
			mSubtitle = binding.subtitle;
			mIndicator = binding.indicator;

			itemView.setOnClickListener(v -> {
				Intent intent = new Intent(context, TrainingActivity.class);
				intent.putExtra("trainingId", id);
				context.startActivity(intent);
			});
		}

		@Override
		public String toString() {
			return super.toString() + " '" + mTitle.getText() + "'";
		}
	}
}