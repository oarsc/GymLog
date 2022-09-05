package org.scp.gymlog.ui.createexercise

import android.app.AlertDialog
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import org.scp.gymlog.R
import org.scp.gymlog.exceptions.LoadException
import org.scp.gymlog.model.Exercise
import org.scp.gymlog.model.ExerciseType
import org.scp.gymlog.model.Muscle
import org.scp.gymlog.model.Variation
import org.scp.gymlog.room.DBThread
import org.scp.gymlog.room.entities.ExerciseEntity
import org.scp.gymlog.room.entities.VariationEntity
import org.scp.gymlog.ui.common.CustomAppCompatActivity
import org.scp.gymlog.ui.common.activity.ImageSelectorActivity
import org.scp.gymlog.ui.common.dialogs.EditTextDialogFragment
import org.scp.gymlog.ui.common.dialogs.EditVariationsDialogFragment
import org.scp.gymlog.ui.common.dialogs.MenuDialogFragment
import org.scp.gymlog.ui.common.dialogs.MenuDialogFragment.Companion.DIALOG_CLOSED
import org.scp.gymlog.util.Constants.IntentReference
import org.scp.gymlog.util.Data
import java.io.IOException
import java.util.regex.Pattern

class CreateExerciseActivity : CustomAppCompatActivity() {

	private var editingExercise: Exercise? = null
	private var name: String = ""
	private var type: ExerciseType = ExerciseType.NONE
	private var imageName: String = ""
	private val muscles: MutableList<Muscle> = ArrayList()
	private val musclesSecondary: MutableList<Muscle> = ArrayList()
	private val variations: MutableList<Variation> = ArrayList()
	private lateinit var iconOption: FormElement
	private lateinit var nameOption: FormElement
	private lateinit var typeOption: FormElement
	private lateinit var musclesOption: FormElement
	private lateinit var musclesSecondaryOption: FormElement
	private val caller: IntentReference by lazy { getIntentCall() }

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_create_exercise)
		setTitle(R.string.title_create_exercise)

		if (caller === IntentReference.EDIT_EXERCISE) {
			editingExercise = Data.getExercise(intent.extras!!.getInt("exerciseId"))
			name = editingExercise!!.name
			type = editingExercise!!.defaultVariation.type
			imageName = editingExercise!!.image
			muscles.addAll(editingExercise!!.primaryMuscles)
			musclesSecondary.addAll(editingExercise!!.secondaryMuscles)
			variations.addAll(editingExercise!!.variations)
		} else {
			if (caller === IntentReference.CREATE_EXERCISE_FROM_MUSCLE) {
				muscles.add(
					Data.getMuscle(intent.extras!!.getInt("muscleId"))
				)
			}
		}

		val recyclerView: RecyclerView = findViewById(R.id.createExerciseFormList)
		recyclerView.layoutManager = LinearLayoutManager(this)
		recyclerView.adapter = CreateExerciseFormRecyclerViewAdapter(createForm())
	}

	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		menuInflater.inflate(R.menu.confirm_menu, menu)
		return true
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		if (item.itemId != R.id.confirmButton) {
			return false
		}

		if (imageName.isBlank()) {
			Snackbar.make(findViewById(android.R.id.content),
				R.string.validation_image, Snackbar.LENGTH_LONG).show()

		} else if (name.isBlank()) {
			Snackbar.make(findViewById(android.R.id.content),
				R.string.validation_name, Snackbar.LENGTH_LONG).show()

		} else if (muscles.isEmpty()) {
			Snackbar.make(findViewById(android.R.id.content),
				R.string.validation_muscles, Snackbar.LENGTH_LONG).show()

		} else {
			val data = Intent()

			if (caller === IntentReference.EDIT_EXERCISE) {
				data.putExtra("exerciseId", editingExercise!!.id)

				editingExercise!!.name = name
				editingExercise!!.defaultVariation.type = type
				editingExercise!!.image = imageName
				editingExercise!!.primaryMuscles.clear()
				editingExercise!!.primaryMuscles.addAll(muscles)
				editingExercise!!.secondaryMuscles.clear()
				editingExercise!!.secondaryMuscles.addAll(musclesSecondary)
				editingExercise!!.variations.clear()
				editingExercise!!.variations.addAll(variations)

				DBThread.run(this) { db ->
					db.exerciseDao().update(editingExercise!!.toEntity())

					// Muscles
					db.exerciseMuscleCrossRefDao()
						.clearMusclesFromExercise(editingExercise!!.id)
					db.exerciseMuscleCrossRefDao()
						.insertAll(editingExercise!!.toMuscleListEntities())
					db.exerciseMuscleCrossRefDao()
						.clearSecondaryMusclesFromExercise(editingExercise!!.id)
					db.exerciseMuscleCrossRefDao()
						.insertAllSecondaries(editingExercise!!.toSecondaryMuscleListEntities())

					// Variations
					db.variationDao().updateAll(
						editingExercise!!.toVariationListEntities().filter { it.variationId > 0 }
					)
					val newVariations = variations
						.filter { it.id == 0 }

					val ids = newVariations
						.map { it.toEntity().apply { exerciseId = editingExercise!!.id } }
						.let { db.variationDao().insertAll(it) }

					newVariations.withIndex().forEach { (index, variation) ->
						variation.id = ids[index].toInt()
					}

					runOnUiThread {
						setResult(RESULT_OK, data)
						finish()
					}
				}

			} else {
				val exercise = Exercise()
				val defaultVariation = Variation(exercise)

				defaultVariation.default = true
				defaultVariation.name = resources.getString(R.string.text_default)
				defaultVariation.type = type
				exercise.variations.add(defaultVariation)

				exercise.name = name
				exercise.image = imageName
				exercise.primaryMuscles.addAll(muscles)
				exercise.secondaryMuscles.addAll(musclesSecondary)
				exercise.variations.addAll(variations)

				DBThread.run(this) { db ->
					val id = db.exerciseDao().insert(exercise.toEntity()).toInt()
					exercise.id = id
					data.putExtra("exerciseId", id)

					// Muscles
					db.exerciseMuscleCrossRefDao()
						.insertAll(exercise.toMuscleListEntities())
					db.exerciseMuscleCrossRefDao()
						.insertAllSecondaries(exercise.toSecondaryMuscleListEntities())

					// Variations
					val ids = db.variationDao().insertAll(exercise.toVariationListEntities())
					variations.withIndex().forEach { (index, variation) ->
						variation.id = ids[index].toInt()
					}

					Data.exercises.add(exercise)
					runOnUiThread {
						setResult(RESULT_OK, data)
						finish()
					}
				}
			}
		}
		return true
	}

	private fun createForm(): List<FormElement> {
		val form: MutableList<FormElement> = ArrayList()

		iconOption = FormElement(
			title = R.string.form_image,
			value = R.string.symbol_empty,
			drawable = if (imageName.isBlank()) null else getIconDrawable(imageName),
			onClickListener = { openImageSelectorActivity() }
		).also(form::add)

		nameOption = FormElement(
			title = R.string.form_name,
			valueStr = name,
			drawable = ResourcesCompat.getDrawable(resources, R.drawable.ic_label_black_24dp, null),
			onClickListener = { showExerciseNameDialog(nameOption) }
		).also(form::add)

		typeOption = FormElement(
			title = R.string.form_type,
			value = type.literal,
			drawable = if (type.icon == 0) null else ResourcesCompat.getDrawable(resources, type.icon, null),
			onClickListener = { showExerciseTypeDialog(typeOption) }
		).also(form::add)

		musclesOption = FormElement(
			title = R.string.form_primary_muscles,
			valueStr = getMusclesLabelText(muscles),
			drawable = ResourcesCompat.getDrawable(resources, R.drawable.ic_body_black_24dp, null),
			onClickListener = { showMuscleSelector(musclesOption, true) }
		).also(form::add)

		musclesSecondaryOption = FormElement(
			title = R.string.form_secondary_muscles,
			valueStr = getMusclesLabelText(musclesSecondary),
			drawable = ResourcesCompat.getDrawable(resources, R.drawable.ic_body_black_24dp, null),
			onClickListener = { showMuscleSelector(musclesSecondaryOption, false) }
		).also(form::add)

		FormElement(
			title = R.string.form_edit_variations,
			value = R.string.symbol_empty,
			drawable = ResourcesCompat.getDrawable(resources, R.drawable.ic_dot_24dp, null),
			onClickListener = { editVariations() }
		).also(form::add)

		return form
	}

	private fun openImageSelectorActivity() {
		val intent = Intent(this, ImageSelectorActivity::class.java)
			.apply { putExtra("title", IntentReference.CREATE_EXERCISE.ordinal) }

		startActivityForResult(intent, IntentReference.IMAGE_SELECTOR)
	}

	override fun onActivityResult(intentReference: IntentReference, data: Intent) {
		if (intentReference === IntentReference.IMAGE_SELECTOR) {
			val fileName = data.getStringExtra("fileName")

			if (fileName != null) {
				val pattern = Pattern.compile(".*?(\\w*)\\.png")
				val matcher = pattern.matcher(fileName)
				if (matcher.matches()) {
					val name = matcher.group(1)

					if (name != null && name.isNotBlank()) {
						imageName = name
						val d = getIconDrawable(imageName)
						iconOption.drawable = d
						iconOption.update()
					}
				}
			}
		}
	}

	private fun showExerciseNameDialog(option: FormElement) {
		val dialog = EditTextDialogFragment(R.string.form_name, name, { result ->
				name = result
				option.valueStr = result
				option.update()
			})
		dialog.show(supportFragmentManager, null)
	}

	private fun showExerciseTypeDialog(option: FormElement) {
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

	private fun showMuscleSelector(option: FormElement, primary: Boolean) {
		val resources = resources
		val allMuscles: List<Muscle> = Data.muscles
		val size = allMuscles.size
		val musclesList = if (primary) muscles else musclesSecondary

		val builder = AlertDialog.Builder(this@CreateExerciseActivity)
		builder.setTitle(if (primary) R.string.form_primary_muscles else R.string.form_secondary_muscles)

		val muscleNames = arrayOfNulls<CharSequence>(size)
		val selectedIndexes = BooleanArray(size)

		for ((idx, muscle) in allMuscles.withIndex()) {
			muscleNames[idx] = resources.getString(muscle.text)
			selectedIndexes[idx] = musclesList.contains(muscle)
		}

		builder.setMultiChoiceItems(muscleNames, selectedIndexes) { _,_,_ -> }
		builder.setNegativeButton(R.string.button_cancel) { _,_ -> }
		builder.setPositiveButton(R.string.button_confirm) { _,_ ->
			musclesList.clear()
			for (i in 0 until size) {
				if (selectedIndexes[i]) {
					musclesList.add(allMuscles[i])
				}
			}
			option.valueStr = getMusclesLabelText(musclesList)
			option.update()
		}
		builder.show()
	}

	private fun editVariations() {
		val dialog = EditVariationsDialogFragment(variations) { editedVariationsList ->
			variations.clear()
			variations.addAll(editedVariationsList)
		}
		dialog.show(supportFragmentManager, null)
	}

	private fun getIconDrawable(imageName: String?): Drawable {
		val fileName = "previews/$imageName.png"
		return try {
			val ims = assets.open(fileName)
			Drawable.createFromStream(ims, null)
		} catch (e: IOException) {
			throw LoadException("Could not read \"$fileName\"", e)
		}
	}

	private fun getMusclesLabelText(muscles: List<Muscle>): String {
		val resources = resources
		return muscles
			.map(Muscle::text)
			.map { strRes -> resources.getString(strRes) }
			.joinToString { it }
	}
}