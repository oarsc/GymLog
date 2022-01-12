package org.scp.gymlog.ui.main.muscles;

import static org.scp.gymlog.ui.common.CustomAppCompatActivity.INTENT_CALLER_ID;

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

import org.json.JSONException;
import org.scp.gymlog.R;
import org.scp.gymlog.room.DBThread;
import org.scp.gymlog.service.DataBaseDumperService;
import org.scp.gymlog.ui.common.components.TrainingFloatingActionButton;
import org.scp.gymlog.ui.createexercise.CreateExerciseActivity;
import org.scp.gymlog.util.Constants.INTENT;

import java.io.IOException;

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
				intent.putExtra(INTENT_CALLER_ID, INTENT.CREATE_EXERCISE);
				startActivity(intent);
				return true;
			} else if (item.getItemId() == R.id.searchButton) {

			} else if (item.getItemId() == R.id.saveButton) {
				DBThread.run(context, db -> {
					try {
						new DataBaseDumperService().save(context, db);
					} catch (JSONException | IOException e) {
						throw new RuntimeException("",e);
					}
				});

			} else if (item.getItemId() == R.id.loadButton) {
				DBThread.run(context, db -> {
					try {
						new DataBaseDumperService().load(context, db);
					} catch (JSONException | IOException e) {
						throw new RuntimeException("",e);
					}
				});

			}

			return false;
		});

		return view;
	}
}