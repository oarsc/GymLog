package org.scp.gymlog.ui.top;

import org.scp.gymlog.util.Constants.IntentReference;
import static org.scp.gymlog.util.Constants.ONE_HUNDRED;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.scp.gymlog.R;
import org.scp.gymlog.exceptions.InternalException;
import org.scp.gymlog.exceptions.LoadException;
import org.scp.gymlog.model.Bit;
import org.scp.gymlog.model.Exercise;
import org.scp.gymlog.room.AppDatabase;
import org.scp.gymlog.room.DBThread;
import org.scp.gymlog.ui.common.DBAppCompatActivity;
import org.scp.gymlog.ui.common.dialogs.EditExercisesLastsDialogFragment;
import org.scp.gymlog.ui.training.TrainingActivity;
import org.scp.gymlog.util.Data;

import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TopActivity extends DBAppCompatActivity {
    private Exercise exercise;
    private List<Bit> topBits;
    private boolean internationalSystem;
    private TopRecyclerViewAdapter adapter;

    @Override
    protected int onLoad(Bundle savedInstanceState, AppDatabase db) {
        int exerciseId = getIntent().getExtras().getInt("exerciseId");
        exercise = Data.getInstance().getExercises().stream()
                .filter(ex -> ex.getId() == exerciseId)
                .findFirst()
                .orElseThrow(() -> new InternalException("Exercise id not found"));

        topBits = getBits(db, exerciseId).collect(Collectors.toList());
        return CONTINUE;
    }

    protected Stream<Bit> getBits(AppDatabase db, int exerciseId) {
        return db.bitDao().findTops(exerciseId).stream()
                .sorted(Comparator.comparing(bit -> -bit.totalWeight))
                .map(bit -> new Bit().fromEntity(bit));
    }

    @Override
    protected void onDelayedCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_tops);
        setTitle(R.string.title_top_records);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        internationalSystem = preferences.getBoolean("internationalSystem", true);

        setHeaderInfo();

        final RecyclerView historyRecyclerView = findViewById(R.id.topList);
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new TopRecyclerViewAdapter(topBits, exercise, internationalSystem);
        historyRecyclerView.setAdapter(adapter);

        adapter.setOnClickListener(this::onElementClicked);
        adapter.setOnLongClickListener(this::onElementLongClicked);
    }

    protected void onElementClicked(Bit topBit) {
        Intent intent = new Intent(this, TrainingActivity.class);
        intent.putExtra("trainingId", topBit.getTrainingId());
        intent.putExtra("focusBit", topBit.getId());
        startActivityForResult(intent, IntentReference.TRAINING);
    }

    protected void onElementLongClicked(Bit topBit) {
        Intent intent = new Intent(this, TopSpecificActivity.class);
        intent.putExtra("exerciseId", topBit.getExerciseId());
        intent.putExtra("weight", topBit.getWeight().getValue().multiply(ONE_HUNDRED).intValue());
        startActivityForResult(intent, IntentReference.TOP_RECORDS);
    }

    public void onActivityResult(IntentReference intentReference, Intent data) {
        if (data.getBooleanExtra("refresh", false)) {
            if (intentReference == IntentReference.TOP_RECORDS) {
                adapter.notifyItemRangeChanged(0, topBits.size());

            } else if (intentReference == IntentReference.TRAINING) {
                DBThread.run(this, db -> {
                    topBits.clear();
                    getBits(db, exercise.getId()).forEach(topBits::add);
                    runOnUiThread(() -> adapter.notifyDataSetChanged());
                });
            }
        }
    }

    private void setHeaderInfo() {
        View fragment = findViewById(R.id.fragmentExercise);
        ImageView image = findViewById(R.id.image);
        TextView time = findViewById(R.id.time);
        TextView title = findViewById(R.id.content);


        title.setText(exercise.getName());
        time.setVisibility(View.GONE);
        String fileName = "previews/" + exercise.getImage() + ".png";
        try {
            InputStream ims = getAssets().open(fileName);
            Drawable d = Drawable.createFromStream(ims, null);
            image.setImageDrawable(d);

        } catch (IOException e) {
            throw new LoadException("Could not read \""+fileName+"\"", e);
        }

        fragment.setOnClickListener(v -> {
            EditExercisesLastsDialogFragment dialog = new EditExercisesLastsDialogFragment(R.string.title_exercises,
                    val -> {
                        if (val != null) {
                            Intent data = new Intent();
                            data.putExtra("refresh", true);
                            setResult(RESULT_OK, data);
                            runOnUiThread(() -> adapter.notifyItemRangeChanged(0, topBits.size()));
                        }
                    },
                    ()->{},
                    exercise, internationalSystem);

            dialog.show(getSupportFragmentManager(), null);
        });
    }
}