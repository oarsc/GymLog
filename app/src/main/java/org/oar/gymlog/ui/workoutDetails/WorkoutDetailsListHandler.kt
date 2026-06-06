package org.oar.gymlog.ui.workoutDetails

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oar.gymlog.databinding.ListitemExercisesRowBinding
import org.oar.gymlog.databinding.ListitemExercisesVariationBinding
import org.oar.gymlog.model.Exercise
import org.oar.gymlog.model.Order
import org.oar.gymlog.model.Variation
import org.oar.gymlog.model.Workout
import org.oar.gymlog.model.WorkoutExercise
import org.oar.gymlog.model.WorkoutSet
import org.oar.gymlog.ui.common.animations.ResizeHeightAnimation
import org.oar.gymlog.ui.common.components.listView.CommonListView.ListElementState
import org.oar.gymlog.ui.common.components.listView.SimpleListHandler
import org.oar.gymlog.ui.common.components.listView.SimpleListView
import org.oar.gymlog.ui.exercises.VariationsListHandler
import org.oar.gymlog.ui.main.preferences.PreferencesDefinition
import org.oar.gymlog.util.Constants.TODAY
import org.oar.gymlog.util.DateUtils.getLetterFrom
import org.oar.gymlog.util.extensions.PreferencesExts.loadString
import org.oar.gymlog.util.extensions.model.ExerciseExts.gymVariations
import java.util.Locale
import java.util.function.BiConsumer
import java.util.function.Consumer


class WorkoutDetailsListHandler(
    val context: Context
): SimpleListHandler<WorkoutExercise, ListitemExercisesRowBinding> {

    override val useListState = false
    override val itemInflater: (LayoutInflater, ViewGroup?, Boolean) -> ListitemExercisesRowBinding
            = ListitemExercisesRowBinding::inflate

    private var onExerciseClickListener: ((WorkoutExercise, Boolean) -> Unit)? = null

    fun onExerciseClicked(listener: (WorkoutExercise, Boolean) -> Unit) {
        onExerciseClickListener = listener
    }
    fun onSetClicked(listener: (WorkoutSet) -> Unit) {

    }

    override fun buildListView(
        binding: ListitemExercisesRowBinding,
        item: WorkoutExercise,
        index: Int,
        state: ListElementState?
    ) {
        val variation = item.variation
        val exercise = variation.exercise

        binding.header.apply {
            exerciseName.text = exercise.name
            if (variation.default) {
                variationName.visibility = View.GONE
            } else {
                variationName.visibility = View.VISIBLE
                variationName.text = variation.name
            }

            image.setImage(exercise.image, exercise.primaryMuscles[0].color)
        }

        binding.variationsList.visibility = View.GONE

        onExerciseClickListener?.also { listener ->
            binding.header.root.setOnLongClickListener {
                listener(item, true)
                true
            }
        }
    }
}