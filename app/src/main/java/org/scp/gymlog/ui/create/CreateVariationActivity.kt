package org.scp.gymlog.ui.create

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import org.scp.gymlog.R
import org.scp.gymlog.model.ExerciseType
import org.scp.gymlog.ui.common.CustomAppCompatActivity
import org.scp.gymlog.ui.common.dialogs.EditTextDialogFragment
import org.scp.gymlog.ui.common.dialogs.MenuDialogFragment
import org.scp.gymlog.ui.common.dialogs.MenuDialogFragment.Companion.DIALOG_CLOSED
import org.scp.gymlog.util.Constants.IntentReference

class CreateVariationActivity : CustomAppCompatActivity() {

	private var variationId = 0
	private var name = ""
	private var type = ExerciseType.NONE

	private lateinit var nameOption: CreateFormElement
	private lateinit var typeOption: CreateFormElement

	private val caller: IntentReference by lazy { getIntentCall() }

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_create)
		setTitle(R.string.title_create_variation)

		intent.extras!!.apply {
			if (caller === IntentReference.EDIT_VARIATION) {
				variationId = getInt("variationId", 0)
			}

			getString("name")?.also { name = it }
			getString("type")?.also { type = ExerciseType.valueOf(it) }
		}

		val recyclerView: RecyclerView = findViewById(R.id.createFormList)
		recyclerView.layoutManager = LinearLayoutManager(this)
		recyclerView.adapter = CreateFormRecyclerViewAdapter(createForm())
	}

	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		menuInflater.inflate(R.menu.confirm_menu, menu)
		return true
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		if (item.itemId != R.id.confirmButton) {
			return false
		}

		if (name.isBlank()) {
			Snackbar.make(findViewById(android.R.id.content),
				R.string.validation_name, Snackbar.LENGTH_LONG).show()

		} else {
			val data = Intent()

			if (caller === IntentReference.EDIT_VARIATION) {
				data.putExtra("variationId", variationId)
			}

			data.putExtra("name", name)
			data.putExtra("type", type.name)

			setResult(RESULT_OK, data)
			finish()
		}
		return true
	}

	private fun createForm(): List<CreateFormElement> {
		val form = mutableListOf<CreateFormElement>()

		nameOption = CreateFormElement(
			title = R.string.form_name,
			valueStr = name,
			drawable = ResourcesCompat.getDrawable(resources, R.drawable.ic_label_black_24dp, null),
			onClickListener = { showExerciseNameDialog(nameOption) }
		).also(form::add)

		typeOption = CreateFormElement(
			title = R.string.form_type,
			value = type.literal,
			drawable = if (type.icon == 0) null else ResourcesCompat.getDrawable(resources, type.icon, null),
			onClickListener = { showExerciseTypeDialog(typeOption) }
		).also(form::add)

		return form
	}

	private fun showExerciseNameDialog(option: CreateFormElement) {
		val dialog = EditTextDialogFragment(R.string.form_name, name, { result ->
				name = result
				option.valueStr = result
				option.update()
			})
		dialog.show(supportFragmentManager, null)
	}

	private fun showExerciseTypeDialog(option: CreateFormElement) {
		val dialog = MenuDialogFragment(R.menu.exercise_type) { result ->
			if (result != DIALOG_CLOSED) {
				type = when(result) {
					R.id.optionDumbbell -> ExerciseType.DUMBBELL
					R.id.optionBarbell -> ExerciseType.BARBELL
					R.id.optionPlate -> ExerciseType.PLATE
					R.id.optionPulley -> ExerciseType.PULLEY_MACHINE
					R.id.optionSmith -> ExerciseType.SMITH_MACHINE
					R.id.optionMachine -> ExerciseType.MACHINE
					R.id.optionCardio -> ExerciseType.CARDIO
					else -> ExerciseType.NONE
				}
				option.value = type.literal
				option.drawable = if (type.icon == 0) null else
					ResourcesCompat.getDrawable(resources, type.icon, null)
				option.update()
			}
		}
		dialog.show(supportFragmentManager, null)
	}
}