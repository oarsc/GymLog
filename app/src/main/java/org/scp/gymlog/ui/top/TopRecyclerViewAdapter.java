package org.scp.gymlog.ui.top;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.scp.gymlog.R;
import org.scp.gymlog.databinding.ListElementFragmentTopBitBinding;
import org.scp.gymlog.databinding.ListElementFragmentTopHeadersBinding;
import org.scp.gymlog.databinding.ListElementFragmentTopSpaceBinding;
import org.scp.gymlog.databinding.ListElementFragmentTopVariationBinding;
import org.scp.gymlog.model.Bit;
import org.scp.gymlog.model.Exercise;
import org.scp.gymlog.ui.top.rows.ITopRow;
import org.scp.gymlog.ui.top.rows.TopBitRow;
import org.scp.gymlog.ui.top.rows.TopVariationRow;
import org.scp.gymlog.util.DateUtils;
import org.scp.gymlog.util.FormatUtils;
import org.scp.gymlog.util.WeightUtils;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;
import java.util.function.Consumer;

public class TopRecyclerViewAdapter extends RecyclerView.Adapter<TopRecyclerViewAdapter.ViewHolder> {

    private final List<ITopRow> rows;
    private final Exercise exercise;
    private final boolean internationalSystem;
    private final Calendar today;

    private Consumer<Bit> onClickListener;
    private Consumer<Bit> onLongClickListener;

    public TopRecyclerViewAdapter(List<ITopRow> rows, Exercise exercise, boolean internationalSystem) {
        this.rows = rows;
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
    public int getItemViewType(int position) {
        switch (rows.get(position).getType()) {
            case BIT:       return R.layout.list_element_fragment_top_bit;
            case VARIATION: return R.layout.list_element_fragment_top_variation;
            case HEADER:    return R.layout.list_element_fragment_top_headers;
            default:        return R.layout.list_element_fragment_top_space;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == R.layout.list_element_fragment_top_bit)
            return new ViewHolder(
                    ListElementFragmentTopBitBinding.inflate(
                            LayoutInflater.from(parent.getContext()), parent, false
                    ));

        if (viewType == R.layout.list_element_fragment_top_variation)
            return new ViewHolder(
                    ListElementFragmentTopVariationBinding.inflate(
                            LayoutInflater.from(parent.getContext()), parent, false
                    ));

        if (viewType == R.layout.list_element_fragment_top_headers)
            return new ViewHolder(
                    ListElementFragmentTopHeadersBinding.inflate(
                            LayoutInflater.from(parent.getContext()), parent, false
                    ));

        return new ViewHolder(
                ListElementFragmentTopSpaceBinding.inflate(
                        LayoutInflater.from(parent.getContext()), parent, false
                ));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        ITopRow row = rows.get(position);

        if (row.getType() == ITopRow.Type.HEADER || row.getType() == ITopRow.Type.SPACE) {
            return;
        }

        if (row.getType() == ITopRow.Type.VARIATION) {
            TopVariationRow vRow = (TopVariationRow) row;
            holder.mNote.setText(vRow.getVariation().getName());
            return;
        }

        TopBitRow bRow = (TopBitRow) row;
        Bit topBit = holder.topBit = bRow.getBit();

        BigDecimal weight = WeightUtils.getWeightFromTotal(
                topBit.getWeight(),
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
        return rows.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public Bit topBit;
        public final TextView mWeight;
        public final TextView mReps;
        public final TextView mTime;
        public final TextView mNote;

        public ViewHolder(ListElementFragmentTopBitBinding binding) {
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

        public ViewHolder(ListElementFragmentTopVariationBinding binding) {
            super(binding.getRoot());
            mWeight = mReps = mTime = null;
            mNote = binding.variationName;
        }

        public ViewHolder(ListElementFragmentTopHeadersBinding binding) {
            super(binding.getRoot());
            mWeight = mReps = mTime = mNote = null;
        }

        public ViewHolder(ListElementFragmentTopSpaceBinding binding) {
            super(binding.getRoot());
            mWeight = mReps = mTime = mNote = null;
        }
    }
}