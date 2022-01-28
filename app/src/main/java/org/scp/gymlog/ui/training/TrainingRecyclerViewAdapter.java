package org.scp.gymlog.ui.training;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.scp.gymlog.R;
import org.scp.gymlog.databinding.ListElementFragmentHistoryBitBinding;
import org.scp.gymlog.databinding.ListElementFragmentHistoryHeadersBinding;
import org.scp.gymlog.databinding.ListElementFragmentHistoryVariationBinding;
import org.scp.gymlog.model.Bit;
import org.scp.gymlog.ui.training.rows.ITrainingRow;
import org.scp.gymlog.ui.training.rows.ITrainingRow.Type;
import org.scp.gymlog.ui.training.rows.TrainingBitRow;
import org.scp.gymlog.ui.training.rows.TrainingVariationRow;
import org.scp.gymlog.util.DateUtils;
import org.scp.gymlog.util.FormatUtils;
import org.scp.gymlog.util.WeightUtils;

import java.math.BigDecimal;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public class TrainingRecyclerViewAdapter extends RecyclerView.Adapter<TrainingRecyclerViewAdapter.ViewHolder> {

    private final ExerciseRows rows;
    private final boolean internationalSystem;

    private BiConsumer<Bit, Integer> onClickListener;

    public TrainingRecyclerViewAdapter(ExerciseRows exerciseRows, boolean internationalSystem) {
        this.rows = exerciseRows;
        this.internationalSystem = internationalSystem;
    }

    public void setOnClickListener(BiConsumer<Bit, Integer> onClickListener) {
        this.onClickListener = onClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        switch (rows.get(position).getType()) {
            case BIT:       return R.layout.list_element_fragment_history_bit;
            case VARIATION: return R.layout.list_element_fragment_history_variation;
            default:        return R.layout.list_element_fragment_history_headers;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == R.layout.list_element_fragment_history_bit)
            return new ViewHolder(
                    ListElementFragmentHistoryBitBinding.inflate(
                            LayoutInflater.from(parent.getContext()), parent, false
                    ));

        if (viewType == R.layout.list_element_fragment_history_variation)
            return new ViewHolder(
                    ListElementFragmentHistoryVariationBinding.inflate(
                            LayoutInflater.from(parent.getContext()), parent, false
                    ));

        return new ViewHolder(
                ListElementFragmentHistoryHeadersBinding.inflate(
                        LayoutInflater.from(parent.getContext()), parent, false
                ));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        ITrainingRow row = rows.get(position);

        if (row.getType() == Type.HEADER) {
            return;
        }

        if (row.getType() == Type.VARIATION) {
            TrainingVariationRow vRow = (TrainingVariationRow) row;
            holder.mNote.setText(vRow.getVariation().getName());
            return;
        }

        TrainingBitRow bRow = holder.bitRow = (TrainingBitRow) row;
        Bit bit = bRow.getBit();

        BigDecimal weight = WeightUtils.getWeightFromTotal(
                bit.getWeight(),
                rows.getExercise().getWeightSpec(),
                rows.getExercise().getBar(),
                internationalSystem
            );

        holder.mWeight.setText(FormatUtils.toString(weight));
        holder.mReps.setText(String.valueOf(bit.getReps()));
        holder.mNote.setText(String.valueOf(bit.getNote()));

        if (bit.isInstant()) {
            holder.mTime.setText(R.string.symbol_empty);
            setAlpha(holder, 0.4f);
        } else {
            holder.mTime.setText(DateUtils.getTime(bit.getTimestamp()));
            setAlpha(holder, 1f);
        }
    }

    private void setAlpha(final ViewHolder holder, final float alpha) {
        Stream.of(holder.mWeight, holder.mReps, holder.mNote)
                .forEach(v -> v.setAlpha(alpha));
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TrainingBitRow bitRow;
        public final TextView mWeight;
        public final TextView mReps;
        public final TextView mTime;
        public final TextView mNote;

        public ViewHolder(ListElementFragmentHistoryBitBinding binding) {
            super(binding.getRoot());
            mWeight = binding.weight;
            mReps = binding.reps;
            mTime = binding.time;
            mNote = binding.note;

            itemView.setOnClickListener(v -> {
                if (onClickListener != null) {
                    onClickListener.accept(bitRow.getBit(), rows.indexOf(bitRow));
                }
            });
        }

        public ViewHolder(ListElementFragmentHistoryVariationBinding binding) {
            super(binding.getRoot());
            mWeight = mReps = mTime = null;
            mNote = binding.variationName;
        }

        public ViewHolder(ListElementFragmentHistoryHeadersBinding binding) {
            super(binding.getRoot());
            mWeight = mReps = mTime = mNote = null;
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mWeight.getText() + "'";
        }
    }
}