package org.scp.gymlog.ui.training;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.scp.gymlog.R;
import org.scp.gymlog.databinding.ListElementFragmentHistoryBitBinding;
import org.scp.gymlog.model.Bit;
import org.scp.gymlog.model.Exercise;
import org.scp.gymlog.util.DateUtils;
import org.scp.gymlog.util.FormatUtils;
import org.scp.gymlog.util.WeightUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.BiConsumer;

public class TrainingBitRecyclerViewAdapter extends RecyclerView.Adapter<TrainingBitRecyclerViewAdapter.ViewHolder> {

    private final List<Bit> bits;
    private final Context context;
    private final boolean internationalSystem;
    private final Exercise exercise;

    private BiConsumer<Bit, Integer> onClickListener;

    public TrainingBitRecyclerViewAdapter(Context context, ExerciseBits exerciseBit, boolean internationalSystem) {
        this.context = context;
        this.bits = exerciseBit.getBits();
        this.exercise = exerciseBit.getExercise();
        this.internationalSystem = internationalSystem;
    }

    public void setOnClickListener(BiConsumer<Bit, Integer> onClickListener) {
        this.onClickListener = onClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(
                ListElementFragmentHistoryBitBinding.inflate(
                        LayoutInflater.from(parent.getContext()), parent, false
                )
        );
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Bit bit = holder.bit = bits.get(position);

        BigDecimal weight = WeightUtils.getWeightFromTotal(
                bit.getWeight().getValue(internationalSystem),
                exercise.getWeightSpec(),
                exercise.getBar(),
                internationalSystem
            );

        holder.mWeight.setText(FormatUtils.toString(weight));
        holder.mReps.setText(String.valueOf(bit.getReps()));
        holder.mNote.setText(String.valueOf(bit.getNote()));

        if (bit.isInstant()) {
            holder.mTime.setText(R.string.symbol_empty);
            holder.itemView.setAlpha(0.4f);
        } else {
            holder.mTime.setText(DateUtils.getTime(bit.getTimestamp()));
        }
    }

    @Override
    public int getItemCount() {
        return bits.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public Bit bit;
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
                    onClickListener.accept(bit, bits.indexOf(bit));
                }
            });
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mWeight.getText() + "'";
        }
    }
}