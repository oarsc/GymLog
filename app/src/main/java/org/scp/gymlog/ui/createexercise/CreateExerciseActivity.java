package org.scp.gymlog.ui.createexercise;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import org.scp.gymlog.R;
import org.scp.gymlog.exceptions.LoadException;
import org.scp.gymlog.model.Exercise;
import org.scp.gymlog.model.Muscle;
import org.scp.gymlog.model.Variation;
import org.scp.gymlog.room.DBThread;
import org.scp.gymlog.room.entities.VariationEntity;
import org.scp.gymlog.ui.common.CustomAppCompatActivity;
import org.scp.gymlog.ui.common.activity.ImageSelectorActivity;
import org.scp.gymlog.ui.common.dialogs.EditNotesDialogFragment;
import org.scp.gymlog.ui.common.dialogs.EditTextDialogFragment;
import org.scp.gymlog.ui.common.dialogs.EditVariationsDialogFragment;
import org.scp.gymlog.util.Constants.IntentReference;
import org.scp.gymlog.util.Data;
import org.scp.gymlog.util.LambdaUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CreateExerciseActivity extends CustomAppCompatActivity {
	private Exercise editingExercise;
	private String name;
	private String imageName;
	private boolean requiresBar;
	private final List<Muscle> muscles = new ArrayList<>();
	private final List<Muscle> musclesSecondary = new ArrayList<>();
	private final List<Variation> variations = new ArrayList<>();

	private FormElement iconOption;
	private FormElement musclesOption;
	private FormElement musclesSecondaryOption;
	private IntentReference caller;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_exercise);
		setTitle(R.string.title_create_exercise);

		caller = getIntentCall();
		if (caller == IntentReference.EDIT_EXERCISE) {
			editingExercise = Data.getExercise(getIntent().getExtras().getInt("exerciseId"));
			name = editingExercise.getName();
			imageName = editingExercise.getImage();
			requiresBar = editingExercise.isRequiresBar();
			muscles.addAll(editingExercise.getPrimaryMuscles());
			musclesSecondary.addAll(editingExercise.getSecondaryMuscles());
			variations.addAll(editingExercise.getVariations());
		} else {
			name = "";
			imageName = "";
			if (caller == IntentReference.CREATE_EXERCISE_FROM_MUSCLE) {
				muscles.add(
						Data.getMuscle(getIntent().getExtras().getInt("muscleId"))
				);
			}
		}

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

		if (imageName == null || imageName.trim().isEmpty()) {
			Snackbar.make(findViewById(android.R.id.content),
					R.string.validation_image, Snackbar.LENGTH_LONG).show();

		} else if (name == null || name.trim().isEmpty()) {
			Snackbar.make(findViewById(android.R.id.content),
					R.string.validation_name, Snackbar.LENGTH_LONG).show();

		} else if (muscles.isEmpty()) {
			Snackbar.make(findViewById(android.R.id.content),
					R.string.validation_muscles, Snackbar.LENGTH_LONG).show();

		} else {
			Intent data = new Intent();

			if (caller == IntentReference.EDIT_EXERCISE) {
				data.putExtra("exerciseId", editingExercise.getId());

				editingExercise.setRequiresBar(requiresBar);
				editingExercise.setName(name);
				editingExercise.setImage(imageName);
				editingExercise.getPrimaryMuscles().clear();
				editingExercise.getPrimaryMuscles().addAll(muscles);
				editingExercise.getSecondaryMuscles().clear();
				editingExercise.getSecondaryMuscles().addAll(musclesSecondary);
				editingExercise.getVariations().clear();
				editingExercise.getVariations().addAll(variations);

				DBThread.run(this, db -> {
					db.exerciseDao().update(editingExercise.toEntity());

					// Muscles
					db.exerciseMuscleCrossRefDao()
							.clearMusclesFromExercise(editingExercise.getId());

					db.exerciseMuscleCrossRefDao()
							.insertAll(editingExercise.toMuscleListEntities());

					db.exerciseMuscleCrossRefDao()
							.clearSecondaryMusclesFromExercise(editingExercise.getId());

					db.exerciseMuscleCrossRefDao()
							.insertAll(editingExercise.toSecondaryMuscleListEntities());

					// Variations
					db.variationDao().updateAll(
							Arrays.stream(editingExercise.toVariationListEntities())
									.filter(v -> v.variationId > 0)
									.toArray(VariationEntity[]::new));

					List<Variation> newVariations = variations.stream()
							.filter(v -> v.getId() == 0)
							.collect(Collectors.toList());

					long[] ids = db.variationDao().insertAll(
							newVariations.stream()
								.map(v -> {
									VariationEntity entity = v.toEntity();
									entity.exerciseId = editingExercise.getId();
									return entity;
								}).toArray(VariationEntity[]::new)
						);
					LambdaUtils.indexForEach(newVariations, (i, v) -> v.setId((int) ids[i]));

					runOnUiThread(() -> {
						setResult(Activity.RESULT_OK, data);
						finish();
					});
				});

			} else {

				final Exercise exercise = new Exercise();
				exercise.setRequiresBar(requiresBar);
				exercise.setName(name);
				exercise.setImage(imageName);
				exercise.getPrimaryMuscles().addAll(muscles);
				exercise.getSecondaryMuscles().addAll(musclesSecondary);
				exercise.getVariations().addAll(variations);

				DBThread.run(this, db -> {
					final int id = (int) db.exerciseDao().insert(exercise.toEntity());
					exercise.setId(id);
					data.putExtra("exerciseId", id);

					// Muscles
					db.exerciseMuscleCrossRefDao()
							.insertAll(exercise.toMuscleListEntities());

					db.exerciseMuscleCrossRefDao()
							.insertAll(exercise.toSecondaryMuscleListEntities());

					// Variations
					long[] ids = db.variationDao()
							.insertAll(exercise.toVariationListEntities());

					LambdaUtils.indexForEach(variations, (i, v) -> v.setId((int) ids[i]));

					Data.getInstance().getExercises().add(exercise);
					runOnUiThread(() -> {
						setResult(Activity.RESULT_OK, data);
						finish();
					});
				});
			}
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
		iconOption.setValue(R.string.symbol_empty);
		if (!imageName.isEmpty()) {
			iconOption.setDrawable(getIconDrawable(imageName));
		}
		iconOption.setOnClickListener(v -> openImageSelectorActivity());

		FormElement nameOption = new FormElement();
		form.add(nameOption);
		nameOption.setDrawable(
				ResourcesCompat.getDrawable(resources, R.drawable.ic_label_black_24dp, null)
		);
		nameOption.setTitle(R.string.form_name);
		nameOption.setValueStr(name);
		nameOption.setOnClickListener(v -> showExerciseNameDialog(nameOption));

		musclesOption = new FormElement();
		form.add(musclesOption);
		musclesOption.setDrawable(
				ResourcesCompat.getDrawable(resources, R.drawable.ic_body_black_24dp, null)
		);
		musclesOption.setTitle(R.string.form_primary_muscles);
		musclesOption.setValueStr(getMusclesLabelText(muscles));
		musclesOption.setOnClickListener(v -> showMuscleSelector(musclesOption, true));

		musclesSecondaryOption = new FormElement();
		form.add(musclesSecondaryOption);
		musclesSecondaryOption.setDrawable(
				ResourcesCompat.getDrawable(resources, R.drawable.ic_body_black_24dp, null)
		);
		musclesSecondaryOption.setTitle(R.string.form_secondary_muscles);
		musclesSecondaryOption.setValueStr(getMusclesLabelText(musclesSecondary));
		musclesSecondaryOption.setOnClickListener(v -> showMuscleSelector(musclesSecondaryOption, false));

		FormElement requiresBarOption = new FormElement();
		form.add(requiresBarOption);
		requiresBarOption.setDrawable(
				ResourcesCompat.getDrawable(resources, requiresBar?
						R.drawable.ic_bar_enable_24dp : R.drawable.ic_bar_disable_24dp, null)
		);
		requiresBarOption.setTitle(R.string.form_requires_bar);
		requiresBarOption.setValue(requiresBar? R.string.form_bar_value :
				R.string.form_no_bar_value);
		requiresBarOption.setOnClickListener(v -> switchEnableBar(requiresBarOption));



		FormElement variationOption = new FormElement();
		form.add(variationOption);
		variationOption.setDrawable(
				ResourcesCompat.getDrawable(resources, R.drawable.ic_dot_24dp, null)
		);
		variationOption.setTitle(R.string.form_edit_variations);
		variationOption.setValue(R.string.symbol_empty);
		variationOption.setOnClickListener(v -> editVariations());

		return form;
	}

	private void openImageSelectorActivity() {
		Intent intent = new Intent(this, ImageSelectorActivity.class);
		intent.putExtra("title", IntentReference.CREATE_EXERCISE.ordinal());
		startActivityForResult(intent, IntentReference.IMAGE_SELECTOR);
	}

	@Override
	public void onActivityResult(IntentReference intentReference, Intent data) {
		if (intentReference == IntentReference.IMAGE_SELECTOR) {
			String fileName = data.getStringExtra("fileName");
			if (fileName != null) {

				Pattern pattern = Pattern.compile(".*?(\\w*)\\.png");
				Matcher matcher = pattern.matcher(fileName);
				if (matcher.matches()) {
					String name = matcher.group(1);

					if (name != null && !name.trim().isEmpty()) {
						imageName = name;
						Drawable d = getIconDrawable(imageName);
						iconOption.setDrawable(d);
						iconOption.update();
					}
				}
			}
		}
	}

	private void showExerciseNameDialog(FormElement option) {
		EditTextDialogFragment dialog = new EditTextDialogFragment(R.string.form_name,
				result -> {
					option.setValueStr(name = result);
					option.update();
				},
				() -> {});
		dialog.setInitialValue(name);
		dialog.show(getSupportFragmentManager(), null);
	}

	private void showMuscleSelector(FormElement option, boolean primary) {
		Resources resources = getResources();
		List<Muscle> allMuscles = Data.getInstance().getMuscles();
		final int size = allMuscles.size();
		final List<Muscle> musclesList = primary? muscles: musclesSecondary;

		AlertDialog.Builder builder = new AlertDialog.Builder(CreateExerciseActivity.this);
		builder.setTitle(primary? R.string.form_primary_muscles : R.string.form_secondary_muscles);

		CharSequence[] muscleNames = new CharSequence[size];
		boolean[] selectedIndexes = new boolean[size];

		int idx = 0;
		for (Muscle muscle : allMuscles) {
			muscleNames[idx] = resources.getString(muscle.getText());
			selectedIndexes[idx] = musclesList.contains(muscle);
			idx++;
		}

		builder.setMultiChoiceItems(muscleNames, selectedIndexes,
				(dialog, which, isChecked) -> {});

		builder.setNegativeButton(R.string.button_cancel, (dialog, which) -> {});
		builder.setPositiveButton(R.string.button_confirm, (dialog, which) -> {
			musclesList.clear();
			for (int i = 0; i<size; i++) {
				if (selectedIndexes[i]) {
					musclesList.add(allMuscles.get(i));
				}
			}

			option.setValueStr(getMusclesLabelText(musclesList));
			option.update();
		});
		builder.show();
	}

	private void switchEnableBar(FormElement option) {
		requiresBar = !requiresBar;

		Drawable drawable = ContextCompat.getDrawable(this, requiresBar?
				R.drawable.ic_bar_enable_24dp :
				R.drawable.ic_bar_disable_24dp);

		option.setDrawable(drawable);
		option.setValue(requiresBar? R.string.form_bar_value :
				R.string.form_no_bar_value);

		option.update();
	}

	private void editVariations() {
		EditVariationsDialogFragment dialog = new EditVariationsDialogFragment(variations,
				editedVariations -> {
					variations.clear();
					variations.addAll(editedVariations);
				});
		dialog.show(getSupportFragmentManager(), null);
	}

	private Drawable getIconDrawable(String imageName) {
		String fileName = "previews/" + imageName + ".png";
		try {
			InputStream ims = getAssets().open(fileName);
			return Drawable.createFromStream(ims, null);
		} catch (IOException e) {
			throw new LoadException("Could not read \""+fileName+"\"", e);
		}
	}

	private String getMusclesLabelText(List<Muscle> muscles) {
		Resources resources = getResources();
		return muscles.stream()
				.map(Muscle::getText)
				.map(resources::getString)
				.collect(Collectors.joining(", "));
	}
}