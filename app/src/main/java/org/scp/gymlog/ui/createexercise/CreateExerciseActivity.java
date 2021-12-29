package org.scp.gymlog.ui.createexercise;

import static org.scp.gymlog.ui.common.activity.ImageSelectorActivity.CREATE_EXERCISE;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import org.scp.gymlog.R;
import org.scp.gymlog.exceptions.LoadException;
import org.scp.gymlog.model.Exercise;
import org.scp.gymlog.model.Muscle;
import org.scp.gymlog.room.DBThread;
import org.scp.gymlog.ui.common.CustomAppCompatActivity;
import org.scp.gymlog.ui.common.activity.ImageSelectorActivity;
import org.scp.gymlog.ui.common.dialogs.EditTextDialogFragment;
import org.scp.gymlog.util.Data;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CreateExerciseActivity extends CustomAppCompatActivity {

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

		exercise = new Exercise();

		RecyclerView recyclerView = findViewById(R.id.createExerciseFormList);
		recyclerView.setLayoutManager(new LinearLayoutManager(this));
		recyclerView.setAdapter(new CreateExerciseFormRecyclerViewAdapter(createForm()));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.confirm_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() != R.id.confirmButton) {
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
			DBThread.run(this, db -> {
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

		FormElement requiresBarOption = new FormElement();
		boolean barIsRequired = exercise.isRequiresBar();
		form.add(requiresBarOption);
		requiresBarOption.setDrawable(
				ResourcesCompat.getDrawable(resources, barIsRequired?
						R.drawable.ic_bar_enable_24dp : R.drawable.ic_bar_disable_24dp, null)
		);
		requiresBarOption.setTitle(R.string.form_requires_bar);
		requiresBarOption.setValue(barIsRequired? R.string.form_bar_value :
				R.string.form_no_bar_value);
		requiresBarOption.setOnClickListener(v -> switchEnableBar(requiresBarOption));

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
		DialogFragment dialog = new EditTextDialogFragment(R.string.form_name,
				result -> {
					option.setValueStr(result);
					option.update();
					exercise.setName(result);
				},
				() -> {});
		dialog.show(getSupportFragmentManager(), null);
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

	private void switchEnableBar(FormElement option) {
		boolean value = !exercise.isRequiresBar();
		exercise.setRequiresBar(value);

		Drawable drawable = ContextCompat.getDrawable(this, value?
				R.drawable.ic_bar_enable_24dp :
				R.drawable.ic_bar_disable_24dp);

		option.setDrawable(drawable);
		option.setValue(value? R.string.form_bar_value :
				R.string.form_no_bar_value);

		option.update();
	}
}