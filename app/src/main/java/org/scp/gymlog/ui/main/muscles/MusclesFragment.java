package org.scp.gymlog.ui.main.muscles;

import static org.scp.gymlog.ui.common.CustomAppCompatActivity.INTENT_CALLER_ID;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.scp.gymlog.R;
import org.scp.gymlog.SplashActivity;
import org.scp.gymlog.room.DBThread;
import org.scp.gymlog.service.DataBaseDumperService;
import org.scp.gymlog.ui.common.components.TrainingFloatingActionButton;
import org.scp.gymlog.service.NotificationService;
import org.scp.gymlog.ui.createexercise.CreateExerciseActivity;
import org.scp.gymlog.util.Constants.INTENT;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

/**
 * A fragment representing a list of Items.
 */
public class MusclesFragment extends Fragment {

	private Context context;
	private final DataBaseDumperService dataBaseDumperService = new DataBaseDumperService();

	@SuppressWarnings("ConstantConditions")
	private final ActivityResultLauncher<Intent> saveResultLauncher = registerForActivityResult(
			new ActivityResultContracts.StartActivityForResult(),
			result -> {
				if (result.getResultCode() == Activity.RESULT_OK) {
					Intent data = result.getData();
					DBThread.run(context, db -> {
						try (FileOutputStream fileOutputStream = (FileOutputStream) context
								.getContentResolver()
								.openOutputStream(data.getData())){

							dataBaseDumperService.save(fileOutputStream, db);
							getActivity().runOnUiThread(() ->
								Toast.makeText(getActivity(), "Saved", Toast.LENGTH_LONG).show());

						} catch (JSONException | IOException e) {
							throw new RuntimeException(e);
						}
					});
				}
			});

	@SuppressWarnings("ConstantConditions")
	private final ActivityResultLauncher<Intent> loadResultLauncher = registerForActivityResult(
			new ActivityResultContracts.StartActivityForResult(),
			result -> {
				if (result.getResultCode() == Activity.RESULT_OK) {
					Intent data = result.getData();

					DBThread.run(context, db -> {
						try (InputStream inputStream = context.getContentResolver()
								.openInputStream(data.getData())){

							dataBaseDumperService.load(inputStream, db);
							Intent intent = new Intent(getActivity(), SplashActivity.class);
							startActivity(intent);
							getActivity().finish();

						} catch (JSONException | IOException e) {
							throw new RuntimeException(e);
						}
					});
				}
			});

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

		TrainingFloatingActionButton fab = view.findViewById(R.id.fabTraining);
		fab.updateFloatingActionButton();

		Toolbar toolbar = view.findViewById(R.id.toolbar);
		toolbar.setOnMenuItemClickListener(item -> {
			if (item.getItemId() == R.id.createButton) {
				Intent intent = new Intent(context, CreateExerciseActivity.class);
				intent.putExtra(INTENT_CALLER_ID, INTENT.CREATE_EXERCISE);
				startActivity(intent);
				return true;
			} else if (item.getItemId() == R.id.searchButton) {
;
			} else if (item.getItemId() == R.id.saveButton) {
				Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
				intent.addCategory(Intent.CATEGORY_OPENABLE);
				intent.setType("application/json");
				intent.putExtra(Intent.EXTRA_TITLE, DataBaseDumperService.OUTPUT);
				saveResultLauncher.launch(intent);

			} else if (item.getItemId() == R.id.loadButton) {
				Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
				intent.addCategory(Intent.CATEGORY_OPENABLE);
				intent.setType("application/json");
				intent.putExtra(Intent.EXTRA_TITLE, DataBaseDumperService.OUTPUT);
				loadResultLauncher.launch(intent);

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
}