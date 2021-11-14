package org.scp.gymlog.ui.main.exercises;

import static android.provider.AlarmClock.EXTRA_MESSAGE;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.scp.gymlog.R;
import org.scp.gymlog.ui.createexercise.CreateExerciseActivity;

/**
 * A fragment representing a list of Items.
 */
public class ExercisesFragment extends Fragment {

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public ExercisesFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_exercise_group_list, container, false);

		Context context = view.getContext();
		RecyclerView recyclerView = view.findViewById(R.id.exercise_list);
		recyclerView.setLayoutManager(new LinearLayoutManager(context));
		recyclerView.setAdapter(new ExercisesRecyclerViewAdapter());

		FloatingActionButton fab = view.findViewById(R.id.fab_add_exercise);
		fab.setOnClickListener(v -> {
				Intent intent = new Intent(context, CreateExerciseActivity.class);
				startActivity(intent);
			});
		return view;
	}
}