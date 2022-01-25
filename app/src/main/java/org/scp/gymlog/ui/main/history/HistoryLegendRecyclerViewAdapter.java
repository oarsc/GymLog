package org.scp.gymlog.ui.main.history;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.scp.gymlog.databinding.ListElementFragmentLegendBinding;
import org.scp.gymlog.model.Muscle;
import org.scp.gymlog.util.Data;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class HistoryLegendRecyclerViewAdapter extends RecyclerView.Adapter<HistoryLegendRecyclerViewAdapter.ViewHolder> {

	private final List<Muscle> muscles;
	private int size;
	private Context context;
	private boolean showingAll = false;

	public HistoryLegendRecyclerViewAdapter() {
		muscles = new ArrayList<>(Data.getInstance().getMuscles());
		size = muscles.size();
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		this.context = parent.getContext();
		return new ViewHolder(
				ListElementFragmentLegendBinding.inflate(
						LayoutInflater.from(this.context), parent, false
				)
		);
	}

	@SuppressLint("NotifyDataSetChanged")
	public void focusMuscles(List<Muscle> focusedMuscles) {
		if (focusedMuscles == null || focusedMuscles.isEmpty()) {
			if (!showingAll) {
				showingAll = true;
				size = muscles.size();
				muscles.sort(Comparator.comparing(Muscle::getId));
				notifyDataSetChanged();
			}

		} else {
			int initSize = getItemCount();
			showingAll = false;
			size = focusedMuscles.size();

			muscles.sort((m1, m2) -> {
				if (focusedMuscles.contains(m1)) {
					if (focusedMuscles.contains(m2))
						return Integer.compare(focusedMuscles.indexOf(m2), focusedMuscles.indexOf(m1));
					else
						return -1;

				} else {
					if (focusedMuscles.contains(m2))
						return 1;
					else
						return Integer.compare(m1.getId(), m2.getId());
				}
			});
			if (size == initSize) {
				notifyItemRangeChanged(0, size);
			} else {
				notifyDataSetChanged();
			}
		}
	}

	@Override
	public void onBindViewHolder(final ViewHolder holder, int position) {
		Muscle muscle = muscles.get(position);
		holder.mText.setText(muscle.getText());
		holder.mIndicator.setCardBackgroundColor(
				ResourcesCompat.getColor(context.getResources(), muscle.getColor(), null));
	}

	@Override
	public int getItemCount() {
		return size;
	}

	public class ViewHolder extends RecyclerView.ViewHolder {
		public Muscle muscle;
		public final TextView mText;
		public final CardView mIndicator;

		public ViewHolder(ListElementFragmentLegendBinding binding) {
			super(binding.getRoot());
			mText = binding.text;
			mIndicator = binding.indicator;
		}

		@Override
		public String toString() {
			return super.toString() + " '" + mText.getText() + "'";
		}
	}
}