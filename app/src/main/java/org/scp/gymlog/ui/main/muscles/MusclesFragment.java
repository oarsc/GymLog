package org.scp.gymlog.ui.main.muscles;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.scp.gymlog.R;
import org.scp.gymlog.SplashActivity;
import org.scp.gymlog.model.Muscle;
import org.scp.gymlog.room.DBThread;
import org.scp.gymlog.service.DataBaseDumperService;
import org.scp.gymlog.service.NotificationService;
import org.scp.gymlog.ui.common.CustomFragment;
import org.scp.gymlog.ui.common.components.TrainingFloatingActionButton;
import org.scp.gymlog.ui.createexercise.CreateExerciseActivity;
import org.scp.gymlog.ui.exercises.ExercisesActivity;
import org.scp.gymlog.util.Constants.IntentReference;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

/**
 * A fragment representing a list of Items.
 */
public class MusclesFragment extends CustomFragment {

	private Context context;
	private final DataBaseDumperService dataBaseDumperService = new DataBaseDumperService();


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
		recyclerView.setAdapter(new MusclesRecyclerViewAdapter(this::onMuscleClicked));

		TrainingFloatingActionButton fab = view.findViewById(R.id.fabTraining);
		fab.updateFloatingActionButton();

		Toolbar toolbar = view.findViewById(R.id.toolbar);
		toolbar.setOnMenuItemClickListener(item -> {
			if (item.getItemId() == R.id.createButton) {
				Intent intent = new Intent(context, CreateExerciseActivity.class);
				startActivity(intent, IntentReference.CREATE_EXERCISE);
				return true;

			} else if (item.getItemId() == R.id.searchButton) {
;
			} else if (item.getItemId() == R.id.saveButton) {
				Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
				intent.addCategory(Intent.CATEGORY_OPENABLE);
				intent.setType("application/json");
				intent.putExtra(Intent.EXTRA_TITLE, DataBaseDumperService.OUTPUT);
				startActivityForResult(intent, IntentReference.SAVE_FILE);

			} else if (item.getItemId() == R.id.loadButton) {
				Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
				intent.addCategory(Intent.CATEGORY_OPENABLE);
				intent.setType("application/json");
				intent.putExtra(Intent.EXTRA_TITLE, DataBaseDumperService.OUTPUT);
				startActivityForResult(intent, IntentReference.LOAD_FILE);

			} else if (item.getItemId() == R.id.testButton) {
				int seconds = 10;
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.SECOND, seconds);
				new NotificationService(context).showNotification(cal, seconds, "Test notification");
			}

			return false;
		});

		return view;
	}

	public void onMuscleClicked(Muscle muscle) {
		Intent intent = new Intent(context, ExercisesActivity.class);
		intent.putExtra("muscleId", muscle.getId());
		startActivity(intent, IntentReference.EXERCISE_LIST);
	}

	@Override
	@SuppressWarnings("ConstantConditions")
	public void onActivityResult(IntentReference intentReference, Intent data) {
		if (intentReference == IntentReference.EXERCISE_LIST) {

		} else if (intentReference == IntentReference.SAVE_FILE) {
			DBThread.run(context, db -> {
				try (FileOutputStream fileOutputStream = (FileOutputStream) context
						.getContentResolver()
						.openOutputStream(data.getData())){

					dataBaseDumperService.save(getContext(), fileOutputStream, db);
					getActivity().runOnUiThread(() ->
							Toast.makeText(getActivity(), "Saved", Toast.LENGTH_LONG).show());

				} catch (JSONException | IOException e) {
					throw new RuntimeException(e);
				}
			});

		} else if (intentReference == IntentReference.LOAD_FILE){
			DBThread.run(context, db -> {
				try (InputStream inputStream = context.getContentResolver()
						.openInputStream(data.getData())){

					dataBaseDumperService.load(getContext(), inputStream, db);
					Intent intent = new Intent(getActivity(), SplashActivity.class);
					startActivity(intent);
					getActivity().finish();

				} catch (JSONException | IOException e) {
					throw new RuntimeException(e);
				}
			});
		}
	}
}