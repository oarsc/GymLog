package org.scp.gymlog.ui.training;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.scp.gymlog.R;
import org.scp.gymlog.databinding.ListElementFragmentHistoryBinding;
import org.scp.gymlog.exceptions.LoadException;
import org.scp.gymlog.model.Bit;
import org.scp.gymlog.model.Exercise;
import org.scp.gymlog.model.Muscle;
import org.scp.gymlog.room.DBThread;
import org.scp.gymlog.ui.common.dialogs.EditBitLogDialogFragment;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TrainingRecyclerViewAdapter extends RecyclerView.Adapter<TrainingRecyclerViewAdapter.ViewHolder> {

    private final List<ExerciseBits> exerciseBits;
    private final List<ViewHolder> holders = new ArrayList<>();
    private final boolean internationalSystem;

    private Consumer<ExerciseBits> onLongClickListener;
    private Consumer<Bit> onBitChangedListener;

    private final Set<Integer> expandedElements = new HashSet<>();

    public TrainingRecyclerViewAdapter(List<ExerciseBits> exerciseBits,
                                       boolean internationalSystem, int focusElement) {
        this.exerciseBits = exerciseBits;
        this.internationalSystem = internationalSystem;
        if (focusElement >= 0) {
            expandedElements.add(focusElement);
        }
    }

    public void setOnLongClickListener(Consumer<ExerciseBits> onLongClickListener) {
        this.onLongClickListener = onLongClickListener;
    }

    public void setOnBitChangedListener(Consumer<Bit> onBitChangedListener) {
        this.onBitChangedListener = onBitChangedListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(
                ListElementFragmentHistoryBinding.inflate(
                        LayoutInflater.from(parent.getContext()), parent, false
                )
        );
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holders.add(holder);
        Context context = holder.itemView.getContext();

        ExerciseBits exerciseBit = holder.exerciseBit = exerciseBits.get(position);

        Exercise exercise = exerciseBit.getExercise();
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


        TrainingBitRecyclerViewAdapter adapter = new TrainingBitRecyclerViewAdapter(exerciseBit, internationalSystem);
        holder.mBitList.setAdapter(adapter);

        adapter.setOnClickListener((bit, index) -> {
            EditBitLogDialogFragment editDialog = new EditBitLogDialogFragment(
                    R.string.title_registry,
                    exercise,
                    exerciseBit.getBits().get(0) != bit,
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
        IntStream.range(0, exerciseBits.size())
                .forEach(expandedElements::add);
        holders.forEach(holder -> holder.toggleBits(true));
    }

    public void collapseAll() {
        expandedElements.clear();
        holders.forEach(holder -> holder.toggleBits(false));
    }

    @Override
    public int getItemCount() {
        return exerciseBits.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ExerciseBits exerciseBit;
        public final TextView mTitle;
        public final TextView mSubtitle;
        public final CardView mIndicator;
        public final ImageView mImage;
        public final RecyclerView mBitList;

        private final LinearLayout bitsContainer;

        public ViewHolder(ListElementFragmentHistoryBinding binding) {
            super(binding.getRoot());
            mTitle = binding.title;
            mSubtitle = binding.subtitle;
            mIndicator = binding.indicator;
            mImage = binding.image;
            mBitList = binding.bitList;

            bitsContainer = binding.bitsContainer;
            binding.header.setOnClickListener(v -> {
                toggleBits();
            });
            binding.header.setOnLongClickListener(v -> {
                if (onLongClickListener != null) {
                    onLongClickListener.accept(exerciseBit);
                    return true;
                }
                return false;
            });
        }

        public void toggleBits() {
            toggleBits(bitsContainer.getVisibility() == View.GONE);
        }

        public void toggleBits(boolean show) {
            if (bitsContainer.getVisibility() == (show? View.GONE : View.VISIBLE)) {
                bitsContainer.setVisibility(show? View.VISIBLE : View.GONE);

                int index = exerciseBits.indexOf(exerciseBit);
                if (show) {
                    expandedElements.add(index);
                } else {
                    expandedElements.remove(index);
                }
            }
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mTitle.getText() + "'";
        }
    }
}