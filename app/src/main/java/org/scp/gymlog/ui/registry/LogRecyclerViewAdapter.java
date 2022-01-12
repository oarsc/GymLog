package org.scp.gymlog.ui.registry;

import static org.scp.gymlog.util.Constants.DATE_ZERO;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.scp.gymlog.R;
import org.scp.gymlog.databinding.ListElementFragmentLogBinding;
import org.scp.gymlog.databinding.ListElementFragmentLogMoreButtonBinding;
import org.scp.gymlog.model.Bit;
import org.scp.gymlog.model.Exercise;
import org.scp.gymlog.util.DateUtils;
import org.scp.gymlog.util.FormatUtils;
import org.scp.gymlog.util.Function;
import org.scp.gymlog.util.WeightUtils;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;
import java.util.function.BiConsumer;

public class LogRecyclerViewAdapter extends RecyclerView.Adapter<LogRecyclerViewAdapter.ViewHolder> {

    private final List<Bit> log;
    private final Calendar today;
    private final int currentTrainingId;
    private final Exercise exercise;
    private BiConsumer<View, Bit> onClickElementListener;
    private Function onLoadMoreListener;
    private boolean fullyLoaded;

    public LogRecyclerViewAdapter(List<Bit> log, Exercise exercise, int currentTrainingId) {
        this.log = log;
        this.today = Calendar.getInstance();
        this.currentTrainingId = currentTrainingId;
        this.exercise = exercise;
    }

    public void setOnClickElementListener(BiConsumer<View, Bit> onClickElementListener) {
        this.onClickElementListener = onClickElementListener;
    }

    public void setOnLoadMoreListener(Function onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    public void setFullyLoaded(boolean fullyLoaded) {
        this.fullyLoaded = fullyLoaded;
        notifyItemRemoved(log.size());
    }

    @Override
    public int getItemViewType(int position) {
        return position == log.size()?
                R.layout.list_element_fragment_log_more_button :
                R.layout.list_element_fragment_log;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == R.layout.list_element_fragment_log_more_button) {
            return new ViewHolder(
                    ListElementFragmentLogMoreButtonBinding.inflate(
                            LayoutInflater.from(parent.getContext()), parent, false
                    )
            );

        } else {
            return new ViewHolder(
                    ListElementFragmentLogBinding.inflate(
                            LayoutInflater.from(parent.getContext()), parent, false
                    )
            );
        }
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        if (holder.loadMoreBtn) {
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
                bit.setSet(bit.isInstant()? lastSet : (lastSet+1));
            } else {
                holder.mDay.setText("T");
                bit.setSet(1);
            }

        } else {
            int[] lastDateDiff = DateUtils.yearsAndDaysDiff(lastDate, bit.getTimestamp());

            if (lastDateDiff[0] != 0 || lastDateDiff[1] != 0) {
                String dayLabel = DateUtils.calculateTimeLetter(bit.getTimestamp(), today);
                holder.mDay.setText(dayLabel);
                bit.setSet(1);
            } else {
                holder.mDay.setText(R.string.symbol_empty);
                if (lastTrainingId == bit.getTrainingId())
                    bit.setSet(bit.isInstant()? lastSet : (lastSet+1));
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
        if (bit.isInstant()) {
            holder.mSet.setText(R.string.symbol_empty);
            holder.itemView.setAlpha(0.4f);
        } else {
            holder.mSet.setText(String.valueOf(bit.getSet()));
        }
        holder.mReps.setText(String.valueOf(bit.getReps()));
        holder.mNotes.setText(bit.getNote());
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
        return fullyLoaded? log.size() : (log.size() + 1); // + load more button
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public Bit bit;
        public TextView mDay, mSet, mWeight, mReps, mNotes;
        public LinearLayout element;
        public int set;
        public boolean loadMoreBtn;

        public ViewHolder(ListElementFragmentLogBinding binding) {
            super(binding.getRoot());
            mDay = binding.day;
            mSet = binding.set;
            mWeight = binding.weight;
            mReps = binding.reps;
            mNotes = binding.notes;
            element = binding.element;

            itemView.setOnClickListener(a-> {
                if (onClickElementListener != null) {
                    onClickElementListener.accept(itemView, bit);
                }
            });
        }

        public ViewHolder(ListElementFragmentLogMoreButtonBinding binding) {
            super(binding.getRoot());
            loadMoreBtn = true;

            itemView.setOnClickListener(a-> {
                if (onLoadMoreListener != null) {
                    onLoadMoreListener.call();
                }
            });
        }


        @Override
        public String toString() {
            return super.toString() + " '" + mDay.getText() + "'";
        }
    }
}