package org.scp.gymlog.ui.main.history;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.scp.gymlog.R;
import org.scp.gymlog.databinding.ListElementFragmentHistoryBinding;
import org.scp.gymlog.model.Muscle;
import org.scp.gymlog.util.DateUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;

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
				ListElementFragmentHistoryBinding.inflate(
						LayoutInflater.from(this.context), parent, false
				)
		);
	}

	@SuppressLint("SetTextI18n")
	@Override
	public void onBindViewHolder(final ViewHolder holder, int position) {
		TrainingData data = trainingDataList.get(position);;
		holder.id = data.id;
		holder.mTitle.setText(context.getResources().getString(R.string.text_training)
				+" #" + data.id + ": "+ context.getResources().getString(R.string.text_started_at)
				+" " + DateUtils.getTime(data.startDate));
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

	public void notifyItemsChanged(int initialSize, int endSize) {
		if (initialSize == endSize) {
			if (initialSize > 0) {
				notifyItemRangeChanged(0, initialSize);
			}
		} else if (initialSize > endSize) {
			if (endSize == 0) {
				notifyItemRangeRemoved(0, initialSize);
			} else {
				notifyItemRangeRemoved(endSize, initialSize-endSize);
				notifyItemRangeChanged(0, endSize);
			}
		} else { // endSize > initialSize
			if (initialSize > 0) {
				notifyItemRangeChanged(0, initialSize);
				notifyItemRangeInserted(initialSize, endSize-initialSize);
			} else {
				notifyItemInserted(endSize);
			}
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

		public ViewHolder(ListElementFragmentHistoryBinding binding) {
			super(binding.getRoot());
			mTitle = binding.title;
			mSubtitle = binding.subtitle;
			mIndicator = binding.indicator;
		}

		@Override
		public String toString() {
			return super.toString() + " '" + mTitle.getText() + "'";
		}
	}

	@Getter @Setter
	public static class TrainingData {
		private List<Muscle> mostUsedMuscles;
		private Calendar startDate;
		private int id;
	}
}