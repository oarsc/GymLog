package org.scp.gymlog.ui.exercises;

import static org.scp.gymlog.util.Constants.INTENT;
import static org.scp.gymlog.util.LambdaUtils.valueEquals;

import android.content.Intent;
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
import org.scp.gymlog.model.Exercise;
import org.scp.gymlog.model.Muscle;
import org.scp.gymlog.model.Order;
import org.scp.gymlog.room.AppDatabase;
import org.scp.gymlog.room.DBThread;
import org.scp.gymlog.ui.common.DBAppCompatActivity;
import org.scp.gymlog.ui.common.components.TrainingFloatingActionButton;
import org.scp.gymlog.ui.common.dialogs.TextDialogFragment;
import org.scp.gymlog.ui.createexercise.CreateExerciseActivity;
import org.scp.gymlog.ui.registry.RegistryActivity;
import org.scp.gymlog.util.Data;

import java.util.List;

public class ExercisesActivity extends DBAppCompatActivity {
    private int muscleId;
    private List<Integer> exercisesId;
    private Order order;
    private ExercisesRecyclerViewAdapter recyclerAdapter;
    private RecyclerView exercisesRecyclerView;

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
        exercisesRecyclerView = findViewById(R.id.exercisesList);
        exercisesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerAdapter = new ExercisesRecyclerViewAdapter(exercisesId, this, order,
                this::onExerciseItemMenuSelected);
        recyclerAdapter.setOnClickListener(ex -> {
            Intent intent = new Intent(this, RegistryActivity.class);
            intent.putExtra("exerciseId", ex.getId());
            startActivityForResult(INTENT.REGISTRY, intent);
        });
        exercisesRecyclerView.setAdapter(recyclerAdapter);

        TrainingFloatingActionButton fab = findViewById(R.id.fabTraining);
        fab.updateFloatingActionButton();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.exercises_menu, menu);

        switch (order) {
            case ALPHABETICALLY: menu.findItem(R.id.sortAlphabetically).setChecked(true); break;
            case LAST_USED:      menu.findItem(R.id.sortLastUsed).setChecked(true);       break;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.create_button) {
            Intent intent = new Intent(this, CreateExerciseActivity.class);
            intent.putExtra("muscleId", muscleId);
            startActivityForResult(INTENT.CREATE_EXERCISE_FROM_MUSCLE, intent);

        } else if (item.getItemId() == R.id.sortAlphabetically) {
            recyclerAdapter.switchOrder(order = Order.ALPHABETICALLY);
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("exercisesSortLastUsed", order.name);
            editor.apply();
            item.setChecked(true);

        } else if (item.getItemId() == R.id.sortLastUsed) {
            recyclerAdapter.switchOrder(order = Order.LAST_USED);
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("exercisesSortLastUsed", order.name);
            editor.apply();
            item.setChecked(true);
        }

        return false;
    }

    private void onExerciseItemMenuSelected(Exercise exercise, int action) {
        if (action == R.id.editExercise) {
            Intent intent = new Intent(this, CreateExerciseActivity.class);
            intent.putExtra("exerciseId", exercise.getId());
            startActivityForResult(INTENT.EDIT_EXERCISE, intent);

        } else if (action == R.id.removeExercise) {
            TextDialogFragment dialog = new TextDialogFragment(R.string.dialog_confirm_remove_exercise_title,
                    R.string.dialog_confirm_remove_exercise_text,
                    confirmed -> {
                        if (confirmed) {
                            DBThread.run(this, db -> {
                                if (db.exerciseDao().delete(exercise.toEntity()) == 1) {
                                    runOnUiThread(() -> recyclerAdapter.removeExercise(exercise));
                                    db.trainingDao().deleteEmptyTraining();

                                    exercisesId.remove((Integer) exercise.getId());
                                    Data.getInstance().getExercises().removeIf(valueEquals(exercise));
                                }
                            });

                        }
                    });
            dialog.show(getSupportFragmentManager(), null);
        }
    }

    @Override
    public void onActivityResult(int intentResultId, Intent data) {
        if (intentResultId == INTENT.EDIT_EXERCISE) {
            int id = data.getIntExtra("exerciseId", -1);
            Exercise ex = Data.getExercise(id);
            boolean hasMuscle = ex.getPrimaryMuscles().stream().map(Muscle::getId)
                    .anyMatch(valueEquals(muscleId));
            if (hasMuscle) {
                recyclerAdapter.updateNotify(ex);
            } else {
                recyclerAdapter.removeExercise(ex);
                exercisesId.removeIf(valueEquals(ex.getId()));
            }

        } else if (intentResultId == INTENT.CREATE_EXERCISE_FROM_MUSCLE) {
            int id = data.getIntExtra("exerciseId", -1);
            Exercise ex = Data.getExercise(id);
            boolean hasMuscle = ex.getPrimaryMuscles().stream().map(Muscle::getId)
                    .anyMatch(valueEquals(muscleId));
            if (hasMuscle) {
                exercisesId.add(ex.getId());
                recyclerAdapter.addExercise(ex);
            }
        } else if (intentResultId == INTENT.REGISTRY) {
            if (data.getBooleanExtra("refresh", false)) {
                if (order.equals(Order.ALPHABETICALLY)) {
                    int id = data.getIntExtra("exerciseId", -1);
                    Exercise ex = Data.getExercise(id);
                    recyclerAdapter.updateNotify(ex);

                } else {
                    exercisesRecyclerView.scrollToPosition(0);
                    recyclerAdapter.switchOrder(order);
                }
            }
        }
    }
}