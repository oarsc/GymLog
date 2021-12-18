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
import org.scp.gymlog.ui.registry.RegistryActivity;
import org.scp.gymlog.util.Data;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

public class ExercisesRecyclerViewAdapter extends RecyclerView.Adapter<ExercisesRecyclerViewAdapter.ViewHolder> {

    private final List<Exercise> exercises;
    private final Context context;

    public ExercisesRecyclerViewAdapter(List<Integer> exercisesList, Context context) {
        Data data = Data.getInstance();
        this.context = context;
        this.exercises = exercisesList.stream()
                .map(id -> Data.getExercise(data, id))
                .collect(Collectors.toList());
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(
                ListElementFragmentBinding.inflate(
                        LayoutInflater.from(parent.getContext()), parent, false
                )
        );
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.exercise = exercises.get(position);
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