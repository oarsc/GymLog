package org.scp.gymlog.ui.registry;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.scp.gymlog.databinding.FragmentListElementLogBinding;
import org.scp.gymlog.model.Bit;
import org.scp.gymlog.util.DateUtils;
import org.scp.gymlog.util.FormatUtils;

import java.util.Calendar;
import java.util.List;

public class LogRecyclerViewAdapter extends RecyclerView.Adapter<LogRecyclerViewAdapter.ViewHolder> {

    private final List<Bit> log;
    private final Calendar today;

    private String lastDayLabel = "";

    public LogRecyclerViewAdapter(List<Bit> log) {
        this.log = log;
        this.today = Calendar.getInstance();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(
                FragmentListElementLogBinding.inflate(
                        LayoutInflater.from(parent.getContext()), parent, false
                )
        );
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Bit bit = holder.bit = log.get(position);

        int[] diff = DateUtils.yearsAndDaysDiff(bit.getTimestamp(), today);
        String dayLabel;
        if (diff[0] == 0) {
            if (diff[1] == 0) {
                dayLabel = "T";
            } else {
                dayLabel = diff[1]+"D";
            }
        } else {
            dayLabel = diff[0]+ "Y" + diff[1]+"D";
        }

        if (!lastDayLabel.equals(dayLabel)) {
            holder.mDay.setText(dayLabel);
            lastDayLabel = dayLabel;
        }

        holder.mSet.setText("1");
        holder.mWeight.setText(FormatUtils.toString(bit.getWeight().getValue()));
        holder.mReps.setText(String.valueOf(bit.getReps()));
        holder.mNotes.setText("+1kg");
    }

    @Override
    public int getItemCount() {
        return log.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public Bit bit;
        public final TextView mDay, mSet, mWeight, mReps, mNotes;

        public ViewHolder(FragmentListElementLogBinding binding) {
            super(binding.getRoot());
            mDay = binding.day;
            mSet = binding.set;
            mWeight = binding.weight;
            mReps = binding.reps;
            mNotes = binding.notes;
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mDay.getText() + "'";
        }
    }
}