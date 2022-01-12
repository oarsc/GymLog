package org.scp.gymlog.ui.exercises;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import org.scp.gymlog.R;
import org.scp.gymlog.databinding.ListElementFragmentExerciseBinding;
import org.scp.gymlog.exceptions.LoadException;
import org.scp.gymlog.model.Exercise;
import org.scp.gymlog.model.Order;
import org.scp.gymlog.ui.common.dialogs.MenuDialogFragment;
import org.scp.gymlog.ui.registry.RegistryActivity;
import org.scp.gymlog.util.Data;
import org.scp.gymlog.util.DateUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ExercisesRecyclerViewAdapter extends RecyclerView.Adapter<ExercisesRecyclerViewAdapter.ViewHolder> {

    private final List<Exercise> exercises;
    private final Context context;
    private final List<Integer> orderedIndexes;
    private final BiConsumer<Exercise, Integer> menuOptionCallback;
    private Order order;
    private final Calendar today;

    private Consumer<Exercise> onClickListener;

    public ExercisesRecyclerViewAdapter(List<Integer> exercisesList, Context context, Order order,
                                        BiConsumer<Exercise, Integer> menuOptionCallback) {
        Data data = Data.getInstance();
        this.today = Calendar.getInstance();
        this.menuOptionCallback = menuOptionCallback;
        this.context = context;
        this.exercises = exercisesList.stream()
                .map(id -> Data.getExercise(data, id))
                .collect(Collectors.toList());

        this.order = order;
        this.orderedIndexes = IntStream.range(0, exercises.size()).boxed()
                .collect(Collectors.toList());
        updateOrder();
    }

    public void setOnClickListener(Consumer<Exercise> onClickListener) {
        this.onClickListener = onClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(
                ListElementFragmentExerciseBinding.inflate(
                        LayoutInflater.from(parent.getContext()), parent, false
                )
        );
    }

    public void removeExercise(Exercise ex) {
        int idx = exercises.indexOf(ex);
        int orderedIdx = orderedIndexes.indexOf(idx);
        exercises.remove(idx);
        orderedIndexes.remove(orderedIdx);
        notifyItemRemoved(orderedIdx);
    }

    public void updateNotify(Exercise ex) {
        int idx = exercises.indexOf(ex);
        int orderedIdx = orderedIndexes.indexOf(idx);
        notifyItemChanged(orderedIdx);
    }

    public void addExercise(Exercise ex) {
        exercises.add(ex);
        int index = orderedIndexes.size();
        orderedIndexes.add(index);
        notifyItemInserted(index);
        switchOrder(order);
    }

    public void switchOrder(Order order) {
        this.order = order;
        updateOrder();
        notifyItemRangeChanged(0, exercises.size());
    }

    private void updateOrder() {
        Comparator<Integer> alphabeticalComparator =
                Comparator.comparing(i -> exercises.get(i).getName().toLowerCase());

        switch(order) {
            case ALPHABETICALLY:
                orderedIndexes.sort(alphabeticalComparator);
                break;

            case LAST_USED:
                Comparator<Integer> comparator =
                        Comparator.comparing(i -> exercises.get(i).getLastTrained());
                orderedIndexes.sort(
                        comparator.reversed().thenComparing(alphabeticalComparator));
                break;
        }
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.exercise = exercises.get(orderedIndexes.get(position));
        holder.mContentView.setText(holder.exercise.getName());

        String timeStr = DateUtils.calculateTimeLetter(holder.exercise.getLastTrained(), today);
        holder.mTime.setVisibility(timeStr.isEmpty()? View.GONE : View.VISIBLE);
        holder.mTime.setText(timeStr);

        String fileName = "previews/" + holder.exercise.getImage() + ".png";
        try {
            InputStream ims = context.getAssets().open(fileName);
            Drawable d = Drawable.createFromStream(ims, null);
            holder.mImageView.setImageDrawable(d);

        } catch (IOException e) {
            throw new LoadException("Could not read \""+fileName+"\"", e);
        }
    }

    @Override
    public int getItemCount() {
        return exercises.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public Exercise exercise;
        public final ImageView mImageView;
        public final TextView mContentView;
        public final TextView mTime;

        public ViewHolder(ListElementFragmentExerciseBinding binding) {
            super(binding.getRoot());
            mContentView = binding.content;
            mImageView = binding.image;
            mTime = binding.time;

            itemView.setOnClickListener(a-> {
                if (onClickListener != null) {
                    onClickListener.accept(exercise);
                }
            });

            itemView.setOnLongClickListener(a-> {
                MenuDialogFragment dialog = new MenuDialogFragment(
                        R.menu.exercise_menu, action -> menuOptionCallback.accept(exercise, action));
                dialog.show(((FragmentActivity) context).getSupportFragmentManager(), null);
                return true;
            });
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}