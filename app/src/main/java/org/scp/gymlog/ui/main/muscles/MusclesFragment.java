package org.scp.gymlog.ui.main.muscles;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.scp.gymlog.R;
import org.scp.gymlog.ui.common.components.TrainingFloatingActionButton;
import org.scp.gymlog.ui.createexercise.CreateExerciseActivity;

/**
 * A fragment representing a list of Items.
 */
public class MusclesFragment extends Fragment {

	private TrainingFloatingActionButton fab;
	private Context context;

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

		context = view.getContext();
		RecyclerView recyclerView = view.findViewById(R.id.musclesList);
		recyclerView.setLayoutManager(new LinearLayoutManager(context));
		recyclerView.setAdapter(new MusclesRecyclerViewAdapter());

		fab = view.findViewById(R.id.fabTraining);
		fab.updateFloatingActionButton();

		Toolbar toolbar = view.findViewById(R.id.toolbar);
		toolbar.setOnMenuItemClickListener(item -> {
			if (item.getItemId() == R.id.createButton) {
				Intent intent = new Intent(context, CreateExerciseActivity.class);
				startActivity(intent);
				return true;
			}
			return false;
		});

		return view;
	}
}