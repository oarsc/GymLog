package org.scp.gymlog.ui.create

import android.app.AlertDialog
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.core.content.res.ResourcesCompat
import org.scp.gymlog.R
import org.scp.gymlog.databinding.ListitemDefaultRowBinding
import org.scp.gymlog.exceptions.LoadException
import org.scp.gymlog.model.*
import org.scp.gymlog.room.Converters
import org.scp.gymlog.ui.common.CustomAppCompatActivity
import org.scp.gymlog.ui.common.activity.ImageSelectorActivity
import org.scp.gymlog.ui.common.components.listView.SimpleListView
import org.scp.gymlog.ui.common.dialogs.EditTextDialogFragment
import org.scp.gymlog.ui.common.dialogs.MenuDialogFragment
import org.scp.gymlog.ui.common.dialogs.MenuDialogFragment.Companion.DIALOG_CLOSED
import org.scp.gymlog.ui.common.dialogs.TextSelectDialogFragment
import org.scp.gymlog.util.Constants.IntentReference
import org.scp.gymlog.util.Data
import org.scp.gymlog.util.extensions.DatabaseExts.dbThread
import org.scp.gymlog.util.extensions.MessagingExts.snackbar
import java.io.IOException
import java.util.regex.Pattern

class CreateExerciseActivity : CustomAppCompatActivity() {

	private lateinit var exercise: Exercise

	private lateinit var iconOption: CreateFormElement
	private lateinit var nameOption: CreateFormElement
	private lateinit var typeOption: CreateFormElement
	private lateinit var gymGlobalOption: CreateFormElement
	private lateinit var musclesOption: CreateFormElement
	private lateinit var musclesSecondaryOption: CreateFormElement
	private val caller: IntentReference by lazy { getIntentCall() }

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_create)
		setTitle(R.string.title_create_exercise)

		if (caller === IntentReference.EDIT_EXERCISE) {
			exercise = intent.extras!!.getInt("exerciseId")
				.let { Data.getExercise(it) }
				.let { Exercise(it) }

		} else {
			exercise = Exercise()
			val defaultVariation = Variation(exercise)
			defaultVariation.default = true
			exercise.variations.add(defaultVariation)

			if (caller === IntentReference.CREATE_EXERCISE_FROM_MUSCLE) {
				exercise.primaryMuscles.add(
					Data.getMuscle(intent.extras!!.getInt("muscleId"))
				)
			}
		}

		findViewById<SimpleListView<CreateFormElement, ListitemDefaultRowBinding>>(R.id.createFormList)
			.init(createForm(), CreateExerciseListHandler)
	}

	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		menuInflater.inflate(R.menu.confirm_menu, menu)
		return true
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		if (item.itemId != R.id.confirmButton) {
			return false
		}

		if (exercise.image.isBlank()) {
			snackbar(R.string.validation_image)

		} else if (exercise.name.isBlank()) {
			snackbar(R.string.validation_name)

		} else if (exercise.primaryMuscles.isEmpty()) {
			snackbar(R.string.validation_muscles)

		} else {
			val data = Intent()

			if (caller === IntentReference.EDIT_EXERCISE) {
				data.putExtra("exerciseId", exercise.id)

				val original = Data.getExercise(exercise.id)

				dbThread { db ->
					db.exerciseDao().update(exercise.toEntity())

					// Muscles
					db.exerciseMuscleCrossRefDao()
						.clearMusclesFromExercise(exercise.id)
					db.exerciseMuscleCrossRefDao()
						.insertAll(exercise.toMuscleListEntities())
					db.exerciseMuscleCrossRefDao()
						.clearSecondaryMusclesFromExercise(exercise.id)
					db.exerciseMuscleCrossRefDao()
						.insertAllSecondaries(exercise.toSecondaryMuscleListEntities())

					// Variations
					exercise.toVariationListEntities()
						.filter { it.variationId > 0 }
						.also { db.variationDao().updateAll(it) }

					val newVariations = exercise.variations
						.filter { it.id == 0 }

					val ids = newVariations
						.map { it.toEntity().apply { exerciseId = exercise.id } }
						.let { db.variationDao().insertAll(it) }

					newVariations.withIndex().forEach { (idx, variation) ->
						variation.id = ids[idx].toInt()
					}

					original.name = exercise.name
					original.image = exercise.image
					original.primaryMuscles.apply {
						clear()
						addAll(exercise.primaryMuscles)
					}

					original.secondaryMuscles.apply {
						clear()
						addAll(exercise.secondaryMuscles)
					}

					exercise.variations
						.map { Variation(it, original) } // update exercise references
						.also {
							original.variations.apply {
								clear()
								addAll(it)
							}
						}

					runOnUiThread {
						setResult(RESULT_OK, data)
						finish()
					}
				}

			} else {

				dbThread { db ->
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
					exercise.variations.withIndex().forEach { (idx, variation) ->
						variation.id = ids[idx].toInt()
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

	private fun createForm(): List<CreateFormElement> {
		val form = mutableListOf<CreateFormElement>()

		iconOption = CreateFormElement(
			title = R.string.form_image,
			value = R.string.symbol_empty,
			drawable = if (exercise.image.isBlank()) null else getIconDrawable(exercise.image),
			onClickListener = { openImageSelectorActivity() }
		).also(form::add)

		nameOption = CreateFormElement(
			title = R.string.form_name,
			valueStr = exercise.name,
			drawable = ResourcesCompat.getDrawable(resources, R.drawable.ic_label_black_24dp, null),
			onClickListener = { showExerciseNameDialog(nameOption) }
		).also(form::add)

		val type = exercise.defaultVariation.type
		typeOption = CreateFormElement(
			title = R.string.form_type,
			value = type.literal,
			drawable = if (type.icon == 0) null else ResourcesCompat.getDrawable(resources, type.icon, null),
			onClickListener = { showExerciseTypeDialog(typeOption) }
		).also(form::add)

		gymGlobalOption = CreateFormElement(
			title = R.string.form_gym_independent,
			value = if (exercise.defaultVariation.gymRelation == GymRelation.NO_RELATION) R.string.text_yes else R.string.text_no,
			drawable = null,
			onClickListener = { toggleGymGlobal(gymGlobalOption) }
		).also(form::add)

		musclesOption = CreateFormElement(
			title = R.string.form_primary_muscles,
			valueStr = getMusclesLabelText(exercise.primaryMuscles),
			drawable = ResourcesCompat.getDrawable(resources, R.drawable.ic_body_black_24dp, null),
			onClickListener = { showMuscleSelector(musclesOption, true) }
		).also(form::add)

		musclesSecondaryOption = CreateFormElement(
			title = R.string.form_secondary_muscles,
			valueStr = getMusclesLabelText(exercise.secondaryMuscles),
			drawable = ResourcesCompat.getDrawable(resources, R.drawable.ic_body_black_24dp, null),
			onClickListener = { showMuscleSelector(musclesSecondaryOption, false) }
		).also(form::add)

		CreateFormElement(
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
		when (intentReference) {
			IntentReference.EDIT_VARIATION -> {
				val variationId = data.getIntExtra("variationId", -1)
				if (variationId > 0) {
					val variation = exercise.variations.firstOrNull { it.id == variationId }!!
					variation.name = data.getStringExtra("name")!!
					variation.type = ExerciseType.valueOf(data.getStringExtra("type")!!)
					val relation = data.getIntExtra("gymRelation", 0)
					variation.gymRelation = Converters.toGymRelation(relation.toShort())
					if (variation.gymRelation == GymRelation.STRICT_RELATION) {
						variation.gymId = Data.currentGym
					}
				}
			}
			IntentReference.CREATE_VARIATION -> {
				val variation = Variation(exercise)
				variation.name = data.getStringExtra("name")!!
				variation.type = ExerciseType.valueOf(data.getStringExtra("type")!!)
				val relation = data.getIntExtra("gymRelation", 0)
				variation.gymRelation = Converters.toGymRelation(relation.toShort())
				if (variation.gymRelation == GymRelation.STRICT_RELATION) {
					variation.gymId = Data.currentGym
				}
				exercise.variations.add(variation)
			}
			IntentReference.IMAGE_SELECTOR -> {
				val fileName = data.getStringExtra("fileName")

				if (fileName != null) {
					val pattern = Pattern.compile(".*?(\\w*)\\.png")
					val matcher = pattern.matcher(fileName)
					if (matcher.matches()) {
						val name = matcher.group(1)

						if (name != null && name.isNotBlank()) {
							exercise.image = name
							iconOption.drawable = getIconDrawable(exercise.image)
							iconOption.update()
						}
					}
				}
			}
			else -> {}
		}
	}

	private fun showExerciseNameDialog(option: CreateFormElement) {
		val dialog = EditTextDialogFragment(R.string.form_name, exercise.name, { result ->
				exercise.name = result
				option.valueStr = result
				option.update()
			})
		dialog.show(supportFragmentManager, null)
	}

	private fun showExerciseTypeDialog(option: CreateFormElement) {
		val dialog = MenuDialogFragment(R.menu.exercise_type) { result ->
			if (result != DIALOG_CLOSED) {
				exercise.defaultVariation.type = when(result) {
					R.id.optionDumbbell -> ExerciseType.DUMBBELL
					R.id.optionBarbell -> ExerciseType.BARBELL
					R.id.optionPlate -> ExerciseType.PLATE
					R.id.optionPulley -> ExerciseType.PULLEY_MACHINE
					R.id.optionSmith -> ExerciseType.SMITH_MACHINE
					R.id.optionMachine -> ExerciseType.MACHINE
					R.id.optionCardio -> ExerciseType.CARDIO
					else -> ExerciseType.NONE
				}
				option.value = exercise.defaultVariation.type.literal
				option.drawable = if (exercise.defaultVariation.type.icon == 0) null else
					ResourcesCompat.getDrawable(resources, exercise.defaultVariation.type.icon, null)
				option.update()
			}
		}
		dialog.show(supportFragmentManager, null)
	}

	private fun toggleGymGlobal(option: CreateFormElement) {
		val newValue = if (exercise.defaultVariation.gymRelation == GymRelation.NO_RELATION)
			GymRelation.INDIVIDUAL_RELATION else GymRelation.NO_RELATION

		exercise.defaultVariation.gymRelation = newValue

		option.value = if (newValue == GymRelation.NO_RELATION) R.string.text_yes else R.string.text_no
		option.update()
	}

	private fun showMuscleSelector(option: CreateFormElement, primary: Boolean) {
		val resources = resources
		val allMuscles: List<Muscle> = Data.muscles
		val size = allMuscles.size
		val musclesList = if (primary) exercise.primaryMuscles else exercise.secondaryMuscles

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
		val options = exercise.gymVariations
			.filter { !it.default }
			.map { it.name }
			.toMutableList()
			.apply { add(resources.getString(R.string.form_create_variation)) }

		val dialog = TextSelectDialogFragment(options) { idx, name ->
			if (idx != DIALOG_CLOSED) {
				if (idx == options.size-1) {
					val intent = Intent(this, CreateVariationActivity::class.java)
					intent.putExtra("type", exercise.defaultVariation.type.name)
					intent.putExtra("gymRelation", exercise.defaultVariation.gymRelation.ordinal)
					startActivityForResult(intent, IntentReference.CREATE_VARIATION)

				} else {
					exercise.variations
						.find { it.name == name }
						?.also {
							val intent = Intent(this, CreateVariationActivity::class.java)
							intent.putExtra("variationId", it.id)
							intent.putExtra("name", it.name)
							intent.putExtra("type", it.type.name)
							intent.putExtra("gymRelation", it.gymRelation.ordinal)
							startActivityForResult(intent, IntentReference.EDIT_VARIATION)
						}
				}
			}
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