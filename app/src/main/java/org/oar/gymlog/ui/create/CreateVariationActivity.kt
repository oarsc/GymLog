package org.oar.gymlog.ui.create

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.core.content.res.ResourcesCompat
import org.oar.gymlog.R
import org.oar.gymlog.databinding.ActivityCreateBinding
import org.oar.gymlog.model.ExerciseType
import org.oar.gymlog.model.GymRelation
import org.oar.gymlog.room.Converters
import org.oar.gymlog.ui.common.BindingAppCompatActivity
import org.oar.gymlog.ui.common.dialogs.EditTextDialogFragment
import org.oar.gymlog.ui.common.dialogs.MenuDialogFragment
import org.oar.gymlog.ui.common.dialogs.MenuDialogFragment.Companion.DIALOG_CLOSED
import org.oar.gymlog.util.Constants.IntentReference
import org.oar.gymlog.util.extensions.MessagingExts.snackbar

class CreateVariationActivity : BindingAppCompatActivity<ActivityCreateBinding>(ActivityCreateBinding::inflate) {

	private var variationId = 0
	private var name = ""
	private var type = ExerciseType.NONE
	private var gymRelation = GymRelation.NO_RELATION

	private lateinit var nameOption: CreateFormElement
	private lateinit var typeOption: CreateFormElement
	private lateinit var gymGlobalOption: CreateFormElement

	private val caller: IntentReference by lazy { getIntentCall() }

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		intent.extras!!.apply {
			if (caller === IntentReference.EDIT_VARIATION) {
				variationId = getInt("variationId", 0)
			}

			getString("name")?.also { name = it }
			getString("type")?.also { type = ExerciseType.valueOf(it) }
			gymRelation = Converters.toGymRelation(getInt("gymRelation", 0).toShort())
		}

		binding.toolbar.apply {
			setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
			setTitle(R.string.title_create_variation)
			setOnMenuItemClickListener(::onOptionsItemSelected)
		}

		binding.createFormList.init(createForm(), CreateExerciseListHandler)
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		if (item.itemId != R.id.confirmButton) {
			return false
		}

		if (name.isBlank()) {
			snackbar(R.string.validation_name)

		} else {
			val data = Intent()

			if (caller === IntentReference.EDIT_VARIATION) {
				data.putExtra("variationId", variationId)
			}

			data.putExtra("name", name)
			data.putExtra("type", type.name)
			data.putExtra("gymRelation", gymRelation.ordinal)

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

		gymGlobalOption = CreateFormElement(
			title = R.string.form_gym_independent,
			value = gymRelation.literal,
			drawable = null,
			onClickListener = { showGymRelationDialog(gymGlobalOption) }
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

	private fun showGymRelationDialog(option: CreateFormElement) {
		val dialog = MenuDialogFragment(R.menu.gym_relation) { result ->
			if (result != DIALOG_CLOSED) {
				gymRelation = when(result) {
					R.id.noRelation -> GymRelation.NO_RELATION
					R.id.individualRelation -> GymRelation.INDIVIDUAL_RELATION
					R.id.strictRelation -> GymRelation.STRICT_RELATION
					else -> GymRelation.NO_RELATION
				}
				option.value = gymRelation.literal
				option.update()
			}
		}
		dialog.show(supportFragmentManager, null)
	}

	private val GymRelation.literal: Int
		get() = when(this) {
			GymRelation.NO_RELATION -> R.string.form_gym_no_relation
			GymRelation.INDIVIDUAL_RELATION -> R.string.form_gym_individual_relation
			GymRelation.STRICT_RELATION -> R.string.form_gym_strict_relation
		}
}