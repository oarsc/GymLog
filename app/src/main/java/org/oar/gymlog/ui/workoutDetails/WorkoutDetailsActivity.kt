package org.oar.gymlog.ui.workoutDetails

import android.os.Bundle
import org.oar.gymlog.databinding.ActivityWorkoutDetailsBinding
import org.oar.gymlog.databinding.ListitemExercisesRowBinding
import org.oar.gymlog.model.Exercise
import org.oar.gymlog.model.Variation
import org.oar.gymlog.model.Workout
import org.oar.gymlog.model.WorkoutExercise
import org.oar.gymlog.model.WorkoutSet
import org.oar.gymlog.ui.common.BindingAppCompatActivity
import org.oar.gymlog.ui.common.components.listView.SimpleListView
import org.oar.gymlog.util.Data

class WorkoutDetailsActivity : BindingAppCompatActivity<ActivityWorkoutDetailsBinding>(ActivityWorkoutDetailsBinding::inflate) {
    private lateinit var workout: Workout
    private lateinit var handler: WorkoutDetailsListHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        workout = intent.extras!!.getInt("workoutId").let(Data::getWorkout)

        binding.toolbar.apply {
            setTitle(workout.name)
            setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
            setOnMenuItemClickListener(::onOptionsItemSelected)
        }

        handler = WorkoutDetailsListHandler(this).apply {
            onExerciseClicked(::itemClicked)
            onSetClicked(::itemClicked)
        }

        val workoutDetailsListView = binding.workoutDetailsList as SimpleListView<WorkoutExercise, ListitemExercisesRowBinding>
        workoutDetailsListView.init(workout.exercises, handler)
    }

    private fun itemClicked(exercise: WorkoutExercise, long: Boolean) {
        if (long) {

        } else {

        }
    }

    private fun itemClicked(variation: WorkoutSet) {

    }
}