package org.scp.gymlog.ui.exercises;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.scp.gymlog.databinding.ListElementFragmentBinding;
import org.scp.gymlog.exceptions.LoadException;
import org.scp.gymlog.model.Exercise;
import org.scp.gymlog.model.Order;
import org.scp.gymlog.ui.registry.RegistryActivity;
import org.scp.gymlog.util.Data;

import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ExercisesRecyclerViewAdapter extends RecyclerView.Adapter<ExercisesRecyclerViewAdapter.ViewHolder> {

    private final List<Exercise> exercises;
    private final Context context;
    private final List<Integer> orderedIndexes;
    private Order order;

    public ExercisesRecyclerViewAdapter(List<Integer> exercisesList, Context context, Order order) {
        Data data = Data.getInstance();
        this.context = context;
        this.exercises = exercisesList.stream()
                .map(id -> Data.getExercise(data, id))
                .collect(Collectors.toList());

        this.order = order;
        this.orderedIndexes = IntStream.range(0, exercises.size()).boxed()
                .collect(Collectors.toList());
        updateOrder();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(
                ListElementFragmentBinding.inflate(
                        LayoutInflater.from(parent.getContext()), parent, false
                )
        );
    }

    public void switchOrder(Order order) {
        this.order = order;
        updateOrder();
        notifyItemRangeChanged(0, exercises.size());
    }

    private void updateOrder() {
        Comparator<Integer> alphabeticalComparator =
                Comparator.comparing(i -> exercises.get(i).getName());

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
        public final TextView mContentView;
        public final ImageView mImageView;

        public ViewHolder(ListElementFragmentBinding binding) {
            super(binding.getRoot());
            mContentView = binding.content;
            mImageView = binding.image;

            binding.getRoot().setOnClickListener(a-> {
                Intent intent = new Intent(context, RegistryActivity.class);
                intent.putExtra("exerciseId", exercise.getId());
                context.startActivity(intent);
            });
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}