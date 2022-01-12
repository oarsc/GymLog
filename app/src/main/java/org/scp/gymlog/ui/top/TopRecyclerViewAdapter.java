package org.scp.gymlog.ui.top;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.scp.gymlog.databinding.ListElementFragmentTopBinding;
import org.scp.gymlog.model.Bit;
import org.scp.gymlog.model.Exercise;
import org.scp.gymlog.ui.main.MainActivity;
import org.scp.gymlog.ui.training.TrainingActivity;
import org.scp.gymlog.util.DateUtils;
import org.scp.gymlog.util.FormatUtils;
import org.scp.gymlog.util.WeightUtils;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;
import java.util.function.Consumer;

public class TopRecyclerViewAdapter extends RecyclerView.Adapter<TopRecyclerViewAdapter.ViewHolder> {

    private final List<Bit> topBits;
    private final Exercise exercise;
    private final boolean internationalSystem;
    private final Calendar today;
    private final Context context;

    private Consumer<Bit> onClickListener;
    private Consumer<Bit> onLongClickListener;

    public TopRecyclerViewAdapter(Context context, List<Bit> topBits, Exercise exercise, boolean internationalSystem) {
        this.context = context;
        this.topBits = topBits;
        this.exercise = exercise;
        this.internationalSystem = internationalSystem;
        this.today = Calendar.getInstance();
    }

    public void setOnClickListener(Consumer<Bit> onClickListener) {
        this.onClickListener = onClickListener;
    }

    public void setOnLongClickListener(Consumer<Bit> onLongClickListener) {
        this.onLongClickListener = onLongClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(
                ListElementFragmentTopBinding.inflate(
                        LayoutInflater.from(parent.getContext()), parent, false
                )
        );
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Bit topBit = holder.topBit = topBits.get(position);

        BigDecimal weight = WeightUtils.getWeightFromTotal(
                topBit.getWeight().getValue(internationalSystem),
                exercise.getWeightSpec(),
                exercise.getBar(),
                internationalSystem
        );

        holder.mWeight.setText(FormatUtils.toString(weight));
        holder.mReps.setText(String.valueOf(topBit.getReps()));
        holder.mTime.setText(DateUtils.getDate(topBit.getTimestamp())+ " ("+
                        DateUtils.calculateTimeLetter(topBit.getTimestamp(), today)+")");

        holder.mNote.setText(topBit.getNote());
    }

    @Override
    public int getItemCount() {
        return topBits.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public Bit topBit;
        public final TextView mWeight;
        public final TextView mReps;
        public final TextView mTime;
        public final TextView mNote;

        public ViewHolder(ListElementFragmentTopBinding binding) {
            super(binding.getRoot());
            mWeight = binding.weight;
            mReps = binding.reps;
            mTime = binding.time;
            mNote = binding.note;

            itemView.setOnClickListener(v -> {
                if (onClickListener != null) {
                    onClickListener.accept(topBit);
                }
            });

            itemView.setOnLongClickListener(v -> {
                if (onLongClickListener != null) {
                    onLongClickListener.accept(topBit);
                }
                return true;
            });
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mWeight.getText() + "'";
        }
    }
}