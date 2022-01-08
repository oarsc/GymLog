package org.scp.gymlog.ui.top;

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
import org.scp.gymlog.ui.common.DBAppCompatActivity;
import org.scp.gymlog.util.Data;

import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class TopActivity extends DBAppCompatActivity {
    private Exercise exercise;
    private List<Bit> topBits;

    @Override
    protected int onLoad(Bundle savedInstanceState, AppDatabase db) {
        int exerciseId = getIntent().getExtras().getInt("exerciseId");
        exercise = Data.getInstance().getExercises().stream()
                .filter(ex -> ex.getId() == exerciseId)
                .findFirst()
                .orElseThrow(() -> new InternalException("Exercise id not found"));

        topBits = db.bitDao().findTops(exerciseId).stream()
                .sorted(Comparator.comparing(bit -> -bit.totalWeight))
                .map(bit -> new Bit().fromEntity(bit))
                .collect(Collectors.toList());

        return CONTINUE;
    }

    @Override
    protected void onDelayedCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_tops);
        setTitle(R.string.title_top_records);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean internationalSystem = preferences.getBoolean("internationalSystem", true);

        setHeaderInfo();

        final RecyclerView historyRecyclerView = findViewById(R.id.topList);
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        historyRecyclerView.setAdapter(new TopRecyclerViewAdapter(this, topBits, exercise, internationalSystem));
    }

    private void setHeaderInfo() {
        View fragment = findViewById(R.id.fragmentExercise);
        ImageView image = findViewById(R.id.image);
        TextView title = findViewById(R.id.content);

        fragment.setClickable(false);

        title.setText(exercise.getName());
        String fileName = "previews/" + exercise.getImage() + ".png";
        try {
            InputStream ims = getAssets().open(fileName);
            Drawable d = Drawable.createFromStream(ims, null);
            image.setImageDrawable(d);

        } catch (IOException e) {
            throw new LoadException("Could not read \""+fileName+"\"", e);
        }
    }
}