package org.oar.gymlog.ui.main.workouts

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import org.oar.gymlog.R
import org.oar.gymlog.databinding.FragmentListWorkoutsBinding
import org.oar.gymlog.databinding.ListitemWorkoutBinding
import org.oar.gymlog.model.Workout
import org.oar.gymlog.ui.LoadActivity
import org.oar.gymlog.ui.common.ResultLauncherFragment
import org.oar.gymlog.ui.common.components.listView.CommonListView
import org.oar.gymlog.ui.common.components.listView.SimpleListHandler
import org.oar.gymlog.ui.workoutDetails.WorkoutDetailsActivity
import org.oar.gymlog.util.Constants.IntentReference
import org.oar.gymlog.util.Data

/**
 * A fragment representing a list of Items.
 */
class WorkoutsFragment : ResultLauncherFragment() {
/**
 * Mandatory empty constructor for the fragment manager to instantiate the
 * fragment (e.g. upon screen orientation changes).
 */

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View = FragmentListWorkoutsBinding.inflate(inflater, container, false)
		.apply {
			toolbar.setNavigationOnClickListener {
				requireActivity().onBackPressedDispatcher.onBackPressed()
			}

			workoutsList.init(Data.workouts, WorkoutListHandler())

			toolbar.setOnMenuItemClickListener { item: MenuItem ->
				when (item.itemId) {
					R.id.testButton -> {
						requireActivity().apply {
							val intent = Intent(this, LoadActivity::class.java)
							intent.action = "keep"
							startActivity(intent)
						}
//                        val seconds = 10
//                        val date = NOW.plusSeconds(seconds.toLong())
//                        NotificationService(requireContext())
//                            .startNewNotification(date, seconds, Data.exercises[0].variations[0])
					}
				}
				false
			}
		}
		.root

	private fun onWorkoutClicked(workout: Workout) {
		val intent = Intent(context, WorkoutDetailsActivity::class.java)
		intent.putExtra("workoutId", workout.id)
		startActivity(intent, IntentReference.WORKOUT_DETAILS)
	}

	inner class WorkoutListHandler : SimpleListHandler<Workout, ListitemWorkoutBinding> {
		override val useListState = false
		override val itemInflater: (LayoutInflater, ViewGroup?, Boolean) -> ListitemWorkoutBinding
				= ListitemWorkoutBinding::inflate

		override fun buildListView(
			binding: ListitemWorkoutBinding,
			item: Workout,
			index: Int,
			state: CommonListView.ListElementState?
		) {
			binding.apply {
				root.setOnClickListener { onWorkoutClicked(item) }
                content.text = item.name
				image.setImageResource(R.drawable.ic_workout_black_24dp)
				image.imageTintList = ColorStateList.valueOf(
					resources.getColor(android.R.color.white, null)
				)
				//indicator.setBackgroundResource(android.R.color.white)
			}
		}
	}
}
