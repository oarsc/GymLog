package org.scp.gymlog.ui.createexercise;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.res.Resources;
import android.os.Bundle;

import org.scp.gymlog.R;
import org.scp.gymlog.model.Data;
import org.scp.gymlog.model.MuscularGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CreateExerciseActivity extends AppCompatActivity {

	private List<MuscularGroup> selectedMuscularGroups = new ArrayList<>();

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
	}

	@Override
	public boolean onSupportNavigateUp() {
		onBackPressed();
		return true;
	}

	private List<FormElement> createForm() {
		List<FormElement> form = new ArrayList<>();

		FormElement option = new FormElement();
		form.add(option);
		option.setTitle(R.string.form_muscle_groups);
		option.setOnClickListener(v -> showMuscleGroupSelector(option));

		return form;
	}

	private void showMuscleGroupSelector(FormElement option) {
		Resources resources = getResources();
		List<MuscularGroup> muscularGroups = Data.getInstance().getGroups();
		final int size = muscularGroups.size();

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