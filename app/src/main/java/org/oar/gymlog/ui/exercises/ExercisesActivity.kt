package org.oar.gymlog.ui.exercises

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import org.oar.gymlog.R
import org.oar.gymlog.databinding.ActivityExercisesBinding
import org.oar.gymlog.databinding.ListitemExercisesRowBinding
import org.oar.gymlog.model.Exercise
import org.oar.gymlog.model.Muscle
import org.oar.gymlog.model.Order
import org.oar.gymlog.model.Order.Companion.getByCode
import org.oar.gymlog.model.Variation
import org.oar.gymlog.ui.common.BindingAppCompatActivity
import org.oar.gymlog.ui.common.components.listView.SimpleListView
import org.oar.gymlog.ui.common.dialogs.MenuDialogFragment
import org.oar.gymlog.ui.common.dialogs.TextDialogFragment
import org.oar.gymlog.ui.create.CreateExerciseActivity
import org.oar.gymlog.ui.main.preferences.PreferencesDefinition
import org.oar.gymlog.ui.registry.RegistryActivity
import org.oar.gymlog.ui.top.TopActivity
import org.oar.gymlog.util.Constants.IntentReference
import org.oar.gymlog.util.Data
import org.oar.gymlog.util.extensions.DatabaseExts.dbThread
import org.oar.gymlog.util.extensions.MessagingExts.toast
import org.oar.gymlog.util.extensions.PreferencesExts.loadString
import org.oar.gymlog.util.extensions.PreferencesExts.save
import org.oar.gymlog.util.extensions.RedirectionExts.goToVariation

class ExercisesActivity : BindingAppCompatActivity<ActivityExercisesBinding>(ActivityExercisesBinding::inflate) {
    private lateinit var muscle: Muscle
    private lateinit var exercises: MutableList<Exercise>

    private lateinit var handler: ExercisesListHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val muscleId = intent.extras!!.getInt("muscleId")
        muscle = Data.getMuscle(muscleId)

        exercises = Data.exercises
            .filter { it.primaryMuscles.contains(muscle) }
            .toMutableList()

        binding.toolbar.apply {
            setTitle(muscle.text)
            setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
            setOnMenuItemClickListener(::onOptionsItemSelected)

            when (getByCode(loadString(PreferencesDefinition.EXERCISES_ORDER))) {
                Order.ALPHABETICALLY -> menu.findItem(R.id.sortAlphabetically).isChecked = true
                Order.LAST_USED -> menu.findItem(R.id.sortLastUsed).isChecked = true
            }
        }

        val exercisesListView = binding.exercisesList as SimpleListView<Exercise, ListitemExercisesRowBinding>
        handler = ExercisesListHandler(this, exercisesListView, muscle)
        exercisesListView.init(exercises, handler)
        handler.init()
        handler.onExerciseClicked(this::itemClicked)
        handler.onVariationClicked(this::itemClicked)

        val expandExerciseId = intent.extras!!.getInt("expandExerciseId", -1)
        if (expandExerciseId > 0) {
            val expandExercise = exercises.find { it.id == expandExerciseId }
            if (expandExercise != null && expandExercise.gymVariations.size > 1) {
                exercisesListView.updateState(expandExercise) {
                    it["expanded"] = true
                    true
                }
            }
        }

        binding.fabTraining.updateFloatingActionButton()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.searchButton -> {
                val intent = Intent(this, SearchActivity::class.java)
                intent.putExtra("muscleId", muscle.id)
                startActivityForResult(intent, IntentReference.SEARCH_LIST)
            }
            R.id.create_button -> {
                val intent = Intent(this, CreateExerciseActivity::class.java)
                intent.putExtra("muscleId", muscle.id)
                startActivityForResult(intent, IntentReference.CREATE_EXERCISE_FROM_MUSCLE)
            }
            R.id.latestButton -> {
                Data.training?.id
                    ?.also { trainingId ->
                        dbThread { db ->
                            db.bitDao()
                                .getMostRecentByTrainingId(trainingId)
                                ?.let { Data.getVariation(it.variationId) }
                                ?.let { goToVariation(it) }
                                ?: toast(R.string.validation_no_exercise_registered)
                        }
                    }
                    ?: toast(R.string.validation_training_not_started)
            }
            R.id.sortAlphabetically -> {
                val order = Order.ALPHABETICALLY
                handler.updateOrder(order)
                save(PreferencesDefinition.EXERCISES_ORDER, order.code)
                item.isChecked = true
            }
            R.id.sortLastUsed -> {
                val order = Order.LAST_USED
                handler.updateOrder(order)
                save(PreferencesDefinition.EXERCISES_ORDER, order.code)
                item.isChecked = true
            }
        }
        return false
    }

    private fun itemClicked(exercise: Exercise, long: Boolean) {
        if (long) {
            val deletionPref = loadString(PreferencesDefinition.EXERCISES_DELETION)
            val removedItems = if (deletionPref == "0") listOf(R.id.removeExercise) else listOf()

            MenuDialogFragment(R.menu.exercise_menu, removedItems) { action ->
                exerciseMenuActionSelected(exercise, action)
            }.apply { show(supportFragmentManager, null) }
        } else {
            val variation = exercise.variations.first { it.default }
            val intent = Intent(this, RegistryActivity::class.java)
            intent.putExtra("exerciseId", exercise.id)
            intent.putExtra("variationId", variation.id)
            startActivityForResult(intent, IntentReference.REGISTRY)
        }
    }

    private fun itemClicked(variation: Variation) {
        val exercise = variation.exercise
        val intent = Intent(this, RegistryActivity::class.java)
        intent.putExtra("exerciseId", exercise.id)
        intent.putExtra("variationId", variation.id)
        startActivityForResult(intent, IntentReference.REGISTRY)
    }

    private fun exerciseMenuActionSelected(exercise: Exercise, action: Int) {
        when (action) {
            R.id.open -> {
                itemClicked(exercise, false)
            }
            R.id.topRanking -> {
                val intent = Intent(this, TopActivity::class.java)
                intent.putExtra("exerciseId", exercise.id)
                startActivity(intent)
            }
            R.id.editExercise -> {
                val intent = Intent(this, CreateExerciseActivity::class.java)
                intent.putExtra("exerciseId", exercise.id)
                startActivityForResult(intent, IntentReference.EDIT_EXERCISE)
            }
            R.id.removeExercise -> {
                val dialog = TextDialogFragment(
                    R.string.dialog_confirm_remove_exercise_title,
                    R.string.dialog_confirm_remove_exercise_text
                ) { confirmed ->
                    if (confirmed) {
                        dbThread { db ->
                            if (db.exerciseDao().delete(exercise.toEntity()) == 1) {
                                runOnUiThread { binding.exercisesList.remove(exercise) }
                                db.trainingDao().deleteEmptyTraining()
                                exercises.remove(exercise)
                                Data.exercises.removeIf { it === exercise }
                            }
                        }
                    }
                }
                dialog.show(supportFragmentManager, null)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.fabTraining.updateFloatingActionButton()
    }

    override fun onActivityResult(intentReference: IntentReference, data: Intent) {
        when (intentReference) {
            IntentReference.EDIT_EXERCISE -> {
                val exerciseId = data.getIntExtra("exerciseId", -1)
                val exercise = Data.getExercise(exerciseId)
                val hasMuscle = exercise.primaryMuscles
                    .any { it.id == muscle.id }

                if (hasMuscle) {
                    binding.exercisesList.notifyUpdate(exercise)
                } else {
                    binding.exercisesList.remove(exercise)
                    exercises.remove(exercise)
                }
            }
            IntentReference.CREATE_EXERCISE_FROM_MUSCLE -> {
                val exerciseId = data.getIntExtra("exerciseId", -1)
                val exercise = Data.getExercise(exerciseId)
                val hasMuscle = exercise.primaryMuscles
                    .any { it.id == muscle.id }

                if (hasMuscle) {
                    exercises.add(exercise)
                    binding.exercisesList.add(exercise)
                }
            }
            IntentReference.SEARCH_LIST -> {
                if (data.getBooleanExtra("refresh", false)) {
                    exercises = Data.exercises
                        .filter { it.primaryMuscles.contains(muscle) }
                        .toMutableList()

                    binding.exercisesList.setListData(exercises)
                }
            }
            IntentReference.REGISTRY -> {
                if (data.getBooleanExtra("refresh", false)) {
                    val order = getByCode(loadString(PreferencesDefinition.EXERCISES_ORDER))

                    if (order == Order.ALPHABETICALLY) {
                        val id = data.getIntExtra("exerciseId", -1)
                        val ex = Data.getExercise(id)
                        binding.exercisesList.notifyUpdate(ex)
                    } else {
                        binding.exercisesList.forceReorder()
                    }
                }
            }
            else -> {}
        }
    }
}