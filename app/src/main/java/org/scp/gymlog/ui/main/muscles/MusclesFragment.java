package org.scp.gymlog.ui.main.muscles;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.scp.gymlog.R;
import org.scp.gymlog.ui.createexercise.CreateExerciseActivity;
import org.scp.gymlog.ui.main.MainActivity;

/**
 * A fragment representing a list of Items.
 */
public class MusclesFragment extends Fragment {

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public MusclesFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_list_muscles, container, false);

		Context context = view.getContext();
		RecyclerView recyclerView = view.findViewById(R.id.muscles_list);
		recyclerView.setLayoutManager(new LinearLayoutManager(context));
		recyclerView.setAdapter(new MusclesRecyclerViewAdapter());

		FloatingActionButton fab = view.findViewById(R.id.fab_add_exercise);
		fab.setOnClickListener(v -> {
				Intent intent = new Intent(context, CreateExerciseActivity.class);
				startActivity(intent);
			});

		Toolbar toolbar = view.findViewById(R.id.toolbar);
		toolbar.setOnMenuItemClickListener(item -> {
			if (item.getItemId() == R.id.create_button) {
				Intent intent = new Intent(context, CreateExerciseActivity.class);
				startActivity(intent);
				return true;
			}
			return false;
		});

		return view;
	}
}