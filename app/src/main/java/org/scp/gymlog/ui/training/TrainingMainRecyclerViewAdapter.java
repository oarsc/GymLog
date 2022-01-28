package org.scp.gymlog.ui.training;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.scp.gymlog.R;
import org.scp.gymlog.databinding.ListElementFragmentHistoryExerciseHeaderBinding;
import org.scp.gymlog.exceptions.LoadException;
import org.scp.gymlog.model.Bit;
import org.scp.gymlog.model.Exercise;
import org.scp.gymlog.model.Muscle;
import org.scp.gymlog.room.DBThread;
import org.scp.gymlog.ui.common.dialogs.EditBitLogDialogFragment;
import org.scp.gymlog.ui.training.rows.TrainingBitRow;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TrainingMainRecyclerViewAdapter extends RecyclerView.Adapter<TrainingMainRecyclerViewAdapter.ViewHolder> {

    private final List<ExerciseRows> exerciseRows;
    private final List<ViewHolder> holders = new ArrayList<>();
    private final boolean internationalSystem;

    private Consumer<ExerciseRows> onLongClickListener;
    private Consumer<Bit> onBitChangedListener;

    private final Set<Integer> expandedElements = new HashSet<>();

    public TrainingMainRecyclerViewAdapter(List<ExerciseRows> exerciseRows,
                                           boolean internationalSystem, int focusElement) {
        this.exerciseRows = exerciseRows;
        this.internationalSystem = internationalSystem;
        if (focusElement >= 0) {
            expandedElements.add(focusElement);
        }
    }

    public void setOnLongClickListener(Consumer<ExerciseRows> onLongClickListener) {
        this.onLongClickListener = onLongClickListener;
    }

    public void setOnBitChangedListener(Consumer<Bit> onBitChangedListener) {
        this.onBitChangedListener = onBitChangedListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(
                ListElementFragmentHistoryExerciseHeaderBinding.inflate(
                        LayoutInflater.from(parent.getContext()), parent, false
                )
        );
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holders.add(holder);
        Context context = holder.itemView.getContext();

        ExerciseRows exerciseRow = holder.exerciseRow = exerciseRows.get(position);

        Exercise exercise = exerciseRow.getExercise();
        holder.mTitle.setText(exercise.getName());
        holder.mSubtitle.setText(
                exercise.getPrimaryMuscles().stream()
                        .map(Muscle::getText)
                        .map(context.getResources()::getString)
                        .collect(Collectors.joining(", "))
            );

        holder.mIndicator.setCardBackgroundColor(
                ResourcesCompat.getColor(context.getResources(),
                        exercise.getPrimaryMuscles().get(0).getColor(), null));

        String fileName = "previews/" + exercise.getImage() + ".png";
        try {
            InputStream ims = context.getAssets().open(fileName);
            Drawable d = Drawable.createFromStream(ims, null);
            holder.mImage.setImageDrawable(d);

        } catch (IOException e) {
            throw new LoadException("Could not read \""+fileName+"\"", e);
        }

        holder.mBitList.setLayoutManager(new LinearLayoutManager(context) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });


        TrainingRecyclerViewAdapter adapter = new TrainingRecyclerViewAdapter(exerciseRow, internationalSystem);
        holder.mBitList.setAdapter(adapter);

        adapter.setOnClickListener((bit, index) -> {
            EditBitLogDialogFragment editDialog = new EditBitLogDialogFragment(
                    R.string.title_registry,
                    exercise,
                    exerciseRow.get(index-1) instanceof TrainingBitRow,
                    internationalSystem,
                    b -> DBThread.run(context, db -> {
                        db.bitDao().update(b.toEntity());
                        ((Activity) context).runOnUiThread(() -> adapter.notifyItemChanged(index));
                        if (onBitChangedListener != null) {
                            onBitChangedListener.accept(b);
                        }
                    })
            );
            editDialog.setInitialValue(bit);
            editDialog.show(((FragmentActivity) context).getSupportFragmentManager(), null);
        });

        holder.toggleBits(expandedElements.contains(position));
    }

    public void expandAll() {
        IntStream.range(0, exerciseRows.size())
                .forEach(expandedElements::add);
        holders.forEach(holder -> holder.toggleBits(true));
    }

    public void collapseAll() {
        expandedElements.clear();
        holders.forEach(holder -> holder.toggleBits(false));
    }

    @Override
    public int getItemCount() {
        return exerciseRows.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ExerciseRows exerciseRow;
        public final TextView mTitle;
        public final TextView mSubtitle;
        public final CardView mIndicator;
        public final ImageView mImage;
        public final RecyclerView mBitList;

        public ViewHolder(ListElementFragmentHistoryExerciseHeaderBinding binding) {
            super(binding.getRoot());
            mTitle = binding.title;
            mSubtitle = binding.subtitle;
            mIndicator = binding.indicator;
            mImage = binding.image;
            mBitList = binding.bitList;

            binding.header.setOnClickListener(v -> {
                toggleBits();
            });
            binding.header.setOnLongClickListener(v -> {
                if (onLongClickListener != null) {
                    onLongClickListener.accept(exerciseRow);
                    return true;
                }
                return false;
            });
        }

        public void toggleBits() {
            int index = exerciseRows.indexOf(exerciseRow);
            toggleBits(!expandedElements.contains(index));
        }

        public void toggleBits(boolean show) {
            mBitList.setVisibility(show? View.VISIBLE : View.GONE);

            int index = exerciseRows.indexOf(exerciseRow);
            if (show) expandedElements.add(index);
            else      expandedElements.remove(index);
        }
        

        @Override
        public String toString() {
            return super.toString() + " '" + mTitle.getText() + "'";
        }
    }
}