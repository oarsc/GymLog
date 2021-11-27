package org.scp.gymlog.ui.createexercise;

import android.app.AlertDialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.InputType;
import android.view.WindowManager;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.scp.gymlog.R;
import org.scp.gymlog.model.Data;
import org.scp.gymlog.model.Exercise;
import org.scp.gymlog.model.MuscularGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CreateExerciseActivity extends AppCompatActivity {

	private Exercise exercise;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_exercise);
		setTitle(R.string.title_create_exercise);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		//getSupportActionBar().setDisplayShowHomeEnabled(true);

		RecyclerView recyclerView = findViewById(R.id.create_exercise_form_list);
		recyclerView.setLayoutManager(new LinearLayoutManager(this));
		recyclerView.setAdapter(new CreateExerciseFormRecyclerViewAdapter(createForm()));

		exercise = new Exercise();
	}

	@Override
	public boolean onSupportNavigateUp() {
		onBackPressed();
		return true;
	}

	private List<FormElement> createForm() {
		List<FormElement> form = new ArrayList<>();

		FormElement nameOption = new FormElement();
		form.add(nameOption);
		nameOption.setDrawable(R.drawable.ic_label_black_24dp);
		nameOption.setTitle(R.string.form_name);
		nameOption.setOnClickListener(v -> showExerciseNameDialog(nameOption));

		FormElement muscleOption = new FormElement();
		form.add(muscleOption);
		muscleOption.setDrawable(R.drawable.ic_body_black_24dp);
		muscleOption.setTitle(R.string.form_muscle_groups);
		muscleOption.setOnClickListener(v -> showMuscleGroupSelector(muscleOption));

		return form;
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

	private void showMuscleGroupSelector(FormElement option) {
		Resources resources = getResources();
		List<MuscularGroup> muscularGroups = Data.getInstance().getGroups();
		final int size = muscularGroups.size();
		final List<MuscularGroup> selectedMuscularGroups = exercise.getBelongingGroups();

		AlertDialog.Builder builder = new AlertDialog.Builder(CreateExerciseActivity.this);
		builder.setTitle(R.string.form_muscle_groups);

		CharSequence[] muscleNames = new CharSequence[size];
		boolean[] selectedIndexes = new boolean[size];

		int idx = 0;
		for (MuscularGroup muscularGroup : muscularGroups) {
			muscleNames[idx] = resources.getString(muscularGroup.getText());
			selectedIndexes[idx] = selectedMuscularGroups.contains(muscularGroup);
			idx++;
		}

		builder.setMultiChoiceItems(muscleNames, selectedIndexes,
				(dialog, which, isChecked) -> {});

		builder.setNegativeButton(R.string.button_cancel, (dialog, which) -> {});
		builder.setPositiveButton(R.string.button_confirm, (dialog, which) -> {
			selectedMuscularGroups.clear();
			List<CharSequence> selectedMuscleNames = new ArrayList<>();
			for (int i = 0; i<size; i++) {
				if (selectedIndexes[i]) {
					selectedMuscularGroups.add(muscularGroups.get(i));
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