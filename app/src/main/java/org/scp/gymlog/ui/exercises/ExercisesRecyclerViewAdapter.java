package org.scp.gymlog.ui.exercises;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.scp.gymlog.databinding.FragmentListElementBinding;
import org.scp.gymlog.exceptions.LoadException;
import org.scp.gymlog.model.Data;
import org.scp.gymlog.model.Exercise;
import org.scp.gymlog.model.MuscularGroup;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

public class ExercisesRecyclerViewAdapter extends RecyclerView.Adapter<ExercisesRecyclerViewAdapter.ViewHolder> {

    private final List<Exercise> exercises;
    private Context context;

    public ExercisesRecyclerViewAdapter(MuscularGroup muscularGroup, Context context) {
        this.context = context;
        this.exercises = Data.getInstance().getExercises().stream()
                .filter(ex -> ex.getBelongingMuscularGroups().contains(muscularGroup))
                .collect(Collectors.toList());
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(
                FragmentListElementBinding.inflate(
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

        public ViewHolder(FragmentListElementBinding binding) {
            super(binding.getRoot());
            mContentView = binding.content;
            mImageView = binding.image;

            binding.getRoot().setOnClickListener(a -> System.out.println("CLICK"));
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}