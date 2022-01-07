package org.scp.gymlog.ui.main.history;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.scp.gymlog.databinding.ListElementFragmentLegendBinding;
import org.scp.gymlog.model.Muscle;
import org.scp.gymlog.util.Data;

import java.util.List;

public class HistoryLegendRecyclerViewAdapter extends RecyclerView.Adapter<HistoryLegendRecyclerViewAdapter.ViewHolder> {

	private final List<Muscle> muscles;
	private Context context;

	public HistoryLegendRecyclerViewAdapter() {
		muscles = Data.getInstance().getMuscles();
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		this.context = parent.getContext();
		return new ViewHolder(
				ListElementFragmentLegendBinding.inflate(
						LayoutInflater.from(this.context), parent, false
				)
		);
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
		return muscles.size();
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