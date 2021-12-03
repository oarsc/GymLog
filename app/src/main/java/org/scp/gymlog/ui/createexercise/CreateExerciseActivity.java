package org.scp.gymlog.ui.createexercise;

import static org.scp.gymlog.ui.tools.ImageSelectorActivity.CREATE_EXERCISE;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.EditText;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import org.scp.gymlog.R;
import org.scp.gymlog.exceptions.LoadException;
import org.scp.gymlog.model.Data;
import org.scp.gymlog.model.Exercise;
import org.scp.gymlog.model.Muscle;
import org.scp.gymlog.room.DBThread;
import org.scp.gymlog.ui.tools.BackAppCompatActivity;
import org.scp.gymlog.ui.tools.ImageSelectorActivity;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CreateExerciseActivity extends BackAppCompatActivity {

	private Exercise exercise;
	private FormElement iconOption;

	private ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
			new ActivityResultContracts.StartActivityForResult(),
			this::captureReturn);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_exercise);
		setTitle(R.string.title_create_exercise);

		RecyclerView recyclerView = findViewById(R.id.create_exercise_form_list);
		recyclerView.setLayoutManager(new LinearLayoutManager(this));
		recyclerView.setAdapter(new CreateExerciseFormRecyclerViewAdapter(createForm()));

		exercise = new Exercise();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.confirm_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() != R.id.confirm_button) {
			return false;
		}

		if (exercise.getImage() == null || exercise.getImage().trim().isEmpty()) {
			Snackbar.make(findViewById(android.R.id.content),
					R.string.validation_image, Snackbar.LENGTH_LONG).show();

		} else if (exercise.getName() == null || exercise.getName().trim().isEmpty()) {
			Snackbar.make(findViewById(android.R.id.content),
					R.string.validation_name, Snackbar.LENGTH_LONG).show();

		} else if (exercise.getBelongingMuscles().isEmpty()) {
			Snackbar.make(findViewById(android.R.id.content),
					R.string.validation_muscles, Snackbar.LENGTH_LONG).show();

		} else {
			new DBThread(this, db -> {
				final int id = (int) db.exerciseDao().insert(exercise.toEntity());
				exercise.setId(id);
				db.exerciseMuscleCrossRefDao()
						.insertAll(exercise.toMuscleListEntities());

				Data.getInstance().getExercises().add(exercise);
				runOnUiThread(this::onBackPressed);
			});
		}

		return true;
	}

	private List<FormElement> createForm() {
		Resources resources = getResources();
		List<FormElement> form = new ArrayList<>();

		iconOption = new FormElement();
		form.add(iconOption);
		//iconOption.setDrawable(R.drawable.ic_label_black_24dp);
		iconOption.setTitle(R.string.form_image);
		iconOption.setValueStr(" ");
		iconOption.setOnClickListener(v -> openImageSelectorActivity());

		FormElement nameOption = new FormElement();
		form.add(nameOption);
		nameOption.setDrawable(
				ResourcesCompat.getDrawable(resources, R.drawable.ic_label_black_24dp, null)
		);
		nameOption.setTitle(R.string.form_name);
		nameOption.setOnClickListener(v -> showExerciseNameDialog(nameOption));

		FormElement muscleOption = new FormElement();
		form.add(muscleOption);
		muscleOption.setDrawable(
				ResourcesCompat.getDrawable(resources, R.drawable.ic_body_black_24dp, null)
		);
		muscleOption.setTitle(R.string.form_muscles);
		muscleOption.setOnClickListener(v -> showMuscleSelector(muscleOption));

		return form;
	}

	private void openImageSelectorActivity() {
		Intent intent = new Intent(this, ImageSelectorActivity.class);
		intent.putExtra("mode", CREATE_EXERCISE);
		someActivityResultLauncher.launch(intent);
	}

	private void captureReturn(ActivityResult result) {
		if (result.getResultCode() == Activity.RESULT_OK) {
			Intent data = result.getData();
			String fileName = data.getStringExtra("fileName");
			if (fileName != null) {

				Pattern pattern = Pattern.compile(".*?(\\w*)\\.png");
				Matcher matcher = pattern.matcher(fileName);
				if (matcher.matches()) {
					String name = matcher.group(1);

					if (name != null && !name.trim().isEmpty()) {
						exercise.setImage(name);
						try {
							InputStream ims = getAssets().open(fileName);
							Drawable d = Drawable.createFromStream(ims, null);
							iconOption.setDrawable(d);
							iconOption.update();
						} catch (IOException e) {
							throw new LoadException("Could not read \""+fileName+"\"", e);
						}
					}
				}
			}
		}
	}

	private void showExerciseNameDialog(FormElement option) {
		AlertDialog.Builder builder = new AlertDialog.Builder(CreateExerciseActivity.this);
		builder.setTitle(R.string.form_name);

		final EditText input = new EditText(this);
		input.setText(exercise.getName());
		input.setInputType(InputType.TYPE_CLASS_TEXT);
		builder.setView(input);

		builder.setNegativeButton(R.string.button_cancel, (dialog, which) -> {});
		builder.setPositiveButton(R.string.button_confirm, (dialog, which) -> {
			String name = input.getText().toString();
			exercise.setName(name);
			option.setValueStr(name);
			option.update();
		});

		AlertDialog dialog = builder.create();
		dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		dialog.show();
		input.requestFocus();
	}

	private void showMuscleSelector(FormElement option) {
		Resources resources = getResources();
		List<Muscle> muscles = Data.getInstance().getMuscles();
		final int size = muscles.size();
		final List<Muscle> selectedMuscles = exercise.getBelongingMuscles();

		AlertDialog.Builder builder = new AlertDialog.Builder(CreateExerciseActivity.this);
		builder.setTitle(R.string.form_muscles);

		CharSequence[] muscleNames = new CharSequence[size];
		boolean[] selectedIndexes = new boolean[size];

		int idx = 0;
		for (Muscle muscle : muscles) {
			muscleNames[idx] = resources.getString(muscle.getText());
			selectedIndexes[idx] = selectedMuscles.contains(muscle);
			idx++;
		}

		builder.setMultiChoiceItems(muscleNames, selectedIndexes,
				(dialog, which, isChecked) -> {});

		builder.setNegativeButton(R.string.button_cancel, (dialog, which) -> {});
		builder.setPositiveButton(R.string.button_confirm, (dialog, which) -> {
			selectedMuscles.clear();
			List<CharSequence> selectedMuscleNames = new ArrayList<>();
			for (int i = 0; i<size; i++) {
				if (selectedIndexes[i]) {
					selectedMuscles.add(muscles.get(i));
					selectedMuscleNames.add(muscleNames[i]);
				}
			}

			option.setValueStr(
					selectedMuscleNames.stream().collect(Collectors.joining(", "))
			);
			option.update();
		});
		builder.show();
	}
}