package org.scp.gymlog.ui.registry;

import static org.scp.gymlog.util.Constants.DATE_ZERO;

import static java.lang.Math.max;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import org.scp.gymlog.R;
import org.scp.gymlog.databinding.ListElementFragmentLogBinding;
import org.scp.gymlog.model.Bit;
import org.scp.gymlog.model.Exercise;
import org.scp.gymlog.util.DateUtils;
import org.scp.gymlog.util.FormatUtils;
import org.scp.gymlog.util.Function;
import org.scp.gymlog.util.WeightUtils;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;
import java.util.function.Consumer;

public class LogRecyclerViewAdapter extends RecyclerView.Adapter<LogRecyclerViewAdapter.ViewHolder> {

    private final List<Bit> log;
    private final Calendar today;
    private final int currentTrainingId;
    private final Exercise exercise;
    private Consumer<Bit> onClickElementListener;

    public LogRecyclerViewAdapter(List<Bit> log, Exercise exercise, int currentTrainingId) {
        this.log = log;
        this.today = Calendar.getInstance();
        this.currentTrainingId = currentTrainingId;
        this.exercise = exercise;
    }

    public void setOnClickElementListener(Consumer<Bit> onClickElementListener) {
        this.onClickElementListener = onClickElementListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(
                ListElementFragmentLogBinding.inflate(
                        LayoutInflater.from(parent.getContext()), parent, false
                )
        );
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        if (position == log.size()) {
            holder.bit = null;
            holder.mDay.setText(R.string.symbol_empty);
            holder.mSet.setText(R.string.symbol_empty);
            holder.mWeight.setText(R.string.symbol_empty);
            holder.mReps.setText(R.string.symbol_empty);
            holder.mNotes.setText(R.string.symbol_empty);
            holder.mLoadMore.setVisibility(View.VISIBLE);
            holder.element.setPadding(0, 40, 0, 40);
            return;
        }

        Bit bit = holder.bit = log.get(position);
        int lastSet = 0;
        Calendar lastDate = DATE_ZERO;
        int lastTrainingId = -1;
        if (position > 0) {
            Bit lastBit = log.get(position-1);
            lastSet = lastBit.getSet();
            lastDate = lastBit.getTimestamp();
            lastTrainingId = lastBit.getTrainingId();
        }

        if (bit.getTrainingId() == currentTrainingId) {
            if (lastTrainingId == currentTrainingId) {
                holder.mDay.setText(R.string.symbol_empty);
                bit.setSet(lastSet+1);
            } else {
                holder.mDay.setText("T");
                bit.setSet(1);
            }

        } else {
            int[] lastDateDiff = DateUtils.yearsAndDaysDiff(lastDate, bit.getTimestamp());

            if (lastDateDiff[0] != 0 || lastDateDiff[1] != 0) {
                int[] todayDiff = DateUtils.yearsAndDaysDiff(bit.getTimestamp(), today);
                String dayLabel;
                if (todayDiff[0] == 0) {
                    if (todayDiff[1] == 0) {
                        dayLabel = "T";
                    } else {
                        dayLabel = todayDiff[1]+"D";
                    }
                } else {
                    dayLabel = todayDiff[0]+ "Y" + todayDiff[1]+"D";
                }

                holder.mDay.setText(dayLabel);
                bit.setSet(1);
            } else {
                holder.mDay.setText(R.string.symbol_empty);
                if (lastTrainingId == bit.getTrainingId())
                    bit.setSet(lastSet+1);
                else
                    bit.setSet(1);
            }
        }

        BigDecimal weight = WeightUtils.getWeightFromTotal(
                bit.getWeight().getValue(),
                exercise.getWeightSpec(),
                exercise.getBar(),
                bit.getWeight().isInternationalSystem()
        );

        holder.mWeight.setText(FormatUtils.toString(weight));
        holder.mSet.setText(String.valueOf(bit.getSet()));
        holder.mReps.setText(String.valueOf(bit.getReps()));
        holder.mNotes.setText(bit.getNote());
        holder.mLoadMore.setVisibility(View.INVISIBLE);
        holder.element.setPadding(0, 0, 0, 0);
    }

    public void notifyTrainingIdChanged(int trainingId, int preIndex) {
        int startIndex = 0;
        int numberOfElements = 0;

        boolean found = false;
        int i = 0;
        for (Bit bitLog : log) {
            if (bitLog.getTrainingId() == trainingId) {
                if (!found) {
                    startIndex = i;
                    found = true;
                }
                numberOfElements++;
            } else if (found) break;
            i++;
        }

        if (numberOfElements > 0) {
            if (preIndex > startIndex && preIndex < (startIndex+numberOfElements))
                notifyItemRangeChanged(preIndex, numberOfElements+startIndex-preIndex);
            else
                notifyItemRangeChanged(startIndex, numberOfElements);
        }
    }

    @Override
    public int getItemCount() {
        return log.size() + 1;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public Bit bit;
        public final TextView mDay, mSet, mWeight, mReps, mNotes, mLoadMore;
        public final ConstraintLayout element;
        public int set;

        public ViewHolder(ListElementFragmentLogBinding binding) {
            super(binding.getRoot());
            mDay = binding.day;
            mSet = binding.set;
            mWeight = binding.weight;
            mReps = binding.reps;
            mNotes = binding.notes;
            mLoadMore = binding.loadMore;
            element = binding.element;

            binding.getRoot().setOnClickListener(a-> {
                if (onClickElementListener != null) {
                    onClickElementListener.accept(bit);
                }
            });
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mDay.getText() + "'";
        }
    }
}