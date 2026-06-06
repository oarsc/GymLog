package org.oar.gymlog.ui.workoutDetails

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oar.gymlog.databinding.ListitemWorkoutDetailsRowBinding
import org.oar.gymlog.databinding.ListitemWorkoutDetailsSetBinding
import org.oar.gymlog.model.WorkoutExercise
import org.oar.gymlog.model.WorkoutSet
import org.oar.gymlog.ui.common.components.listView.CommonListView.ListElementState
import org.oar.gymlog.ui.common.components.listView.SimpleListHandler
import org.oar.gymlog.ui.common.components.listView.SimpleListView

class WorkoutDetailsListHandler(
    val context: Context
): SimpleListHandler<WorkoutExercise, ListitemWorkoutDetailsRowBinding> {
    override val useListState = false
    override val itemInflater: (LayoutInflater, ViewGroup?, Boolean) -> ListitemWorkoutDetailsRowBinding
            = ListitemWorkoutDetailsRowBinding::inflate

    private var onExerciseClickListener: ((WorkoutExercise, Boolean) -> Unit)? = null

    fun onExerciseClicked(listener: (WorkoutExercise, Boolean) -> Unit) {
        onExerciseClickListener = listener
    }
    fun onSetClicked(listener: (WorkoutSet) -> Unit) {
        //WorkoutDetailsSetListHandler.onSetClicked(listener)
    }

    override fun buildListView(
        binding: ListitemWorkoutDetailsRowBinding,
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

        val variationList =
            binding.setsList as SimpleListView<WorkoutSet, ListitemWorkoutDetailsSetBinding>

        val handler = WorkoutDetailsSetListHandler(item)
        variationList.init(item.sets, handler)

        onExerciseClickListener?.also { listener ->
            binding.header.root.setOnLongClickListener {
                listener(item, true)
                true
            }
        }
        onExerciseClickListener?.also { listener ->
            binding.header.root.setOnClickListener { listener(item, false) }
        }
    }
}