package org.scp.gymlog.ui.exercises;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.scp.gymlog.R;
import org.scp.gymlog.exceptions.InternalException;
import org.scp.gymlog.model.Muscle;
import org.scp.gymlog.model.Order;
import org.scp.gymlog.room.AppDatabase;
import org.scp.gymlog.ui.common.DBAppCompatActivity;
import org.scp.gymlog.ui.common.components.TrainingFloatingActionButton;
import org.scp.gymlog.util.Data;

import java.util.List;

public class ExercisesActivity extends DBAppCompatActivity {
    private int muscleId;
    private List<Integer> exercisesId;
    private Order order;
    private ExercisesRecyclerViewAdapter recyclerAdapter;

    @Override
    protected int onLoad(Bundle savedInstanceState, AppDatabase db) {
        muscleId = getIntent().getExtras().getInt("muscleId");
        exercisesId = db.exerciseDao().getExercisesIdByMuscleId(muscleId);
        return CONTINUE;
    }

    @Override
    protected void onDelayedCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_exercises);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        order = Order.getByName(
                preferences.getString("exercisesSortLastUsed", Order.ALPHABETICALLY.name));

        Muscle muscle = Data.getInstance().getMuscles().stream()
                .filter(gr -> gr.getId() == muscleId)
                .findFirst()
                .orElseThrow(() -> new InternalException("Muscle id not found"));

        setTitle(muscle.getText());
        RecyclerView recyclerView = findViewById(R.id.exercisesList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerAdapter = new ExercisesRecyclerViewAdapter(exercisesId, this, order);
        recyclerView.setAdapter(recyclerAdapter);

        TrainingFloatingActionButton fab = findViewById(R.id.fabTraining);
        fab.updateFloatingActionButton();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.exercises_menu, menu);

        if (order.equals(Order.ALPHABETICALLY)) {
            menu.findItem(R.id.sorting).setIcon(R.drawable.ic_sort_24dp);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.sorting) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = preferences.edit();

            if (order.equals(Order.ALPHABETICALLY)) {
                order = Order.LAST_USED;
                editor.putString("exercisesSortLastUsed", order.name);
                item.setIcon(R.drawable.ic_sort_alphabetically_24dp);
                item.setTitle(R.string.sort_alphabetically);

            } else {
                order = Order.ALPHABETICALLY;
                editor.putString("exercisesSortLastUsed", order.name);
                item.setIcon(R.drawable.ic_sort_24dp);
                item.setTitle(R.string.sort_last_used);
            }
            recyclerAdapter.switchOrder(order);
            editor.apply();
            return false;

        }
        return false;
    }
}