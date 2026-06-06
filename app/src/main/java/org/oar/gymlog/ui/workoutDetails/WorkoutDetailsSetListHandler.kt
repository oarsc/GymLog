package org.oar.gymlog.ui.workoutDetails

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oar.gymlog.databinding.ListitemWorkoutDetailsSetBinding
import org.oar.gymlog.model.WorkoutExercise
import org.oar.gymlog.model.WorkoutSet
import org.oar.gymlog.ui.common.components.listView.CommonListView.ListElementState
import org.oar.gymlog.ui.common.components.listView.SimpleListHandler

class WorkoutDetailsSetListHandler(
    workoutExercise: WorkoutExercise
): SimpleListHandler<WorkoutSet, ListitemWorkoutDetailsSetBinding> {
    private var onSetClickListener: ((WorkoutSet) -> Unit)? = null
    override val useListState = false
    override val itemInflater: (LayoutInflater, ViewGroup?, Boolean) -> ListitemWorkoutDetailsSetBinding
            = ListitemWorkoutDetailsSetBinding::inflate

    private val nonWarmUpSets = workoutExercise.sets.filter { !it.warmUp }

    @SuppressLint("SetTextI18n")
    override fun buildListView(
        binding: ListitemWorkoutDetailsSetBinding,
        item: WorkoutSet,
        index: Int,
        state: ListElementState?
    ) {
        binding.root.alpha = if (item.warmUp) 0.5f else 1f
        binding.reps.text = item.reps.toString()
        binding.note.text = item.note

        val idx = nonWarmUpSets.indexOf(item)
        binding.index.text = if (idx < 0) "" else "#${idx+1}"

        binding.image.visibility = View.GONE

        onSetClickListener?.also { listener ->
            binding.root.setOnClickListener { listener(item) }
        }
    }

    fun onSetClicked(listener: (WorkoutSet) -> Unit) { onSetClickListener = listener }
}
