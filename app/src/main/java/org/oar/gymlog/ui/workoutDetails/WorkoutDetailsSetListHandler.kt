package org.oar.gymlog.ui.workoutDetails

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import org.oar.gymlog.R
import org.oar.gymlog.databinding.ListitemWorkoutDetailsSetBinding
import org.oar.gymlog.databinding.ListitemWorkoutDetailsSetHeaderBinding
import org.oar.gymlog.model.WorkoutExercise
import org.oar.gymlog.model.WorkoutSet
import org.oar.gymlog.ui.common.components.listView.CommonListView.ListElementState
import org.oar.gymlog.ui.common.components.listView.MultipleListHandler
import org.oar.gymlog.ui.workoutDetails.rows.IWorkoutSetRow
import org.oar.gymlog.ui.workoutDetails.rows.WorkoutSetHeaderRow
import org.oar.gymlog.ui.workoutDetails.rows.WorkoutSetRow

class WorkoutDetailsSetListHandler(
    workoutExercise: WorkoutExercise
): MultipleListHandler<IWorkoutSetRow> {
    private var onSetClickListener: ((WorkoutSet) -> Unit)? = null
    override val useListState = false
    private val nonWarmUpSets = workoutExercise.sets.filter { !it.warmUp }

    override val itemInflaters = listOf<(LayoutInflater, ViewGroup?, Boolean) -> ViewBinding>(
        ListitemWorkoutDetailsSetBinding::inflate,
        ListitemWorkoutDetailsSetHeaderBinding::inflate,
    )

    override fun findItemInflaterIndex(item: IWorkoutSetRow) = if (item is WorkoutSetRow) 0 else 1

    @SuppressLint("SetTextI18n")
    override fun buildListView(
        binding: ViewBinding,
        item: IWorkoutSetRow,
        index: Int,
        state: ListElementState?
    ) {
        when (item) {
            is WorkoutSetHeaderRow -> {
                binding as ListitemWorkoutDetailsSetHeaderBinding
                binding.imageTitle.visibility = View.GONE

            }
            is WorkoutSetRow -> {
                binding as ListitemWorkoutDetailsSetBinding

                val set = item.workoutSet

                binding.root.alpha = if (set.warmUp) 0.5f else 1f
                binding.reps.text = set.reps.toString()
                binding.note.text = set.note

                val idx = nonWarmUpSets.indexOf(set)
                if (idx < 0)
                    binding.index.setText(R.string.text_warm_up)
                else
                    binding.index.text ="#${idx+1}"

                binding.image.visibility = View.GONE

                onSetClickListener?.also { listener ->
                    binding.root.setOnClickListener { listener(set) }
                }
            }
        }
    }

    fun onSetClicked(listener: (WorkoutSet) -> Unit) { onSetClickListener = listener }
}
