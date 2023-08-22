package org.scp.gymlog.ui.main.muscles

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.scp.gymlog.R
import org.scp.gymlog.SplashActivity
import org.scp.gymlog.model.Muscle
import org.scp.gymlog.room.DBThread
import org.scp.gymlog.room.entities.GymEntity
import org.scp.gymlog.service.DataBaseDumperService
import org.scp.gymlog.service.NotificationService
import org.scp.gymlog.ui.common.CustomFragment
import org.scp.gymlog.ui.common.components.TrainingFloatingActionButton
import org.scp.gymlog.ui.common.dialogs.EditTextDialogFragment
import org.scp.gymlog.ui.common.dialogs.MenuDialogFragment.Companion.DIALOG_CLOSED
import org.scp.gymlog.ui.common.dialogs.TextSelectDialogFragment
import org.scp.gymlog.ui.create.CreateExerciseActivity
import org.scp.gymlog.ui.exercises.ExercisesActivity
import org.scp.gymlog.ui.latest.LatestExercisesActivity
import org.scp.gymlog.util.Constants.IntentReference
import org.scp.gymlog.util.Data
import org.scp.gymlog.util.DateUtils.currentDateTime
import org.scp.gymlog.util.PreferencesUtils.save


/**
 * A fragment representing a list of Items.
 */
class MusclesFragment : CustomFragment() {
/**
 * Mandatory empty constructor for the fragment manager to instantiate the
 * fragment (e.g. upon screen orientation changes).
 */
	private val dataBaseDumperService by lazy { DataBaseDumperService() }
	private var trainingFloatingButton: TrainingFloatingActionButton? = null

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		val view = inflater.inflate(R.layout.fragment_list_muscles, container, false)

		val recyclerView = view.findViewById<RecyclerView>(R.id.musclesList)
		recyclerView.layoutManager = LinearLayoutManager(context)
		recyclerView.adapter = MusclesRecyclerViewAdapter { muscle -> onMuscleClicked(muscle) }

		trainingFloatingButton = view.findViewById(R.id.fabTraining)

		val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
		toolbar.setOnMenuItemClickListener { item: MenuItem ->
			when (item.itemId) {
				R.id.createButton -> {
					val intent = Intent(context, CreateExerciseActivity::class.java)
					startActivity(intent, IntentReference.CREATE_EXERCISE)
					return@setOnMenuItemClickListener true
				}
				R.id.searchButton -> {
				}
				R.id.latestButton -> {
					requireActivity().apply {
						val intent = Intent(this, LatestExercisesActivity::class.java)
						startActivity(intent)
					}
				}
				R.id.gymSelectButton -> {
					val context = requireContext()

					DBThread.run(context) { db ->
						val gyms = db.gymDao().getAll()
						val labels = gyms
							.map(GymEntity::name)
							.toMutableList()
							.apply { add("Add new gym") }
						val dialog = TextSelectDialogFragment(labels, Data.currentGym - 1) { idx, _ ->
							if (idx != DIALOG_CLOSED) {
								if (idx == gyms.size) {
									val dialog = EditTextDialogFragment(R.string.dialog_write_new_gym, confirm = {
										DBThread.run(context) { db ->
											db.gymDao().insert(GymEntity(name = it))
											val gymId = idx + 1;
											context.save("gym", gymId)
											Data.currentGym = gymId

											requireActivity().apply {
												val intent = Intent(this, SplashActivity::class.java)
												startActivity(intent)
												finish()
											}
										}
									})
									dialog.show(requireActivity().supportFragmentManager, null)

								} else {
									val gymId = idx + 1;
									context.save("gym", gymId)
									Data.currentGym = gymId

									requireActivity().apply {
										val intent = Intent(this, SplashActivity::class.java)
										startActivity(intent)
										finish()
									}
								}
							}
						}
						dialog.show(requireActivity().supportFragmentManager, null)
					}
				}
				R.id.exportButton -> {
					val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
					intent.addCategory(Intent.CATEGORY_OPENABLE)
					intent.type = "application/json"
					intent.putExtra(Intent.EXTRA_TITLE, DataBaseDumperService.OUTPUT)
					startActivityForResult(intent, IntentReference.EXPORT_FILE)
				}
				R.id.importButton -> {
					val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
					intent.addCategory(Intent.CATEGORY_OPENABLE)
					intent.type = "application/json"
					intent.putExtra(Intent.EXTRA_TITLE, DataBaseDumperService.OUTPUT)
					startActivityForResult(intent, IntentReference.IMPORT_FILE)
				}
				R.id.testButton -> {
					val seconds = 2
					val date = currentDateTime().plusSeconds(seconds.toLong())
					NotificationService(requireContext())
						.showNotification(date, seconds, "Test notification")
				}
			}
			false
		}
		return view
	}

	private fun onMuscleClicked(muscle: Muscle) {
		val intent = Intent(context, ExercisesActivity::class.java)
		intent.putExtra("muscleId", muscle.id)
		startActivity(intent, IntentReference.EXERCISE_LIST)
	}

	override fun onActivityResult(intentReference: IntentReference, data: Intent) {
		when {
			intentReference === IntentReference.EXERCISE_LIST -> {}
			intentReference === IntentReference.EXPORT_FILE ->
				requireActivity().apply {
					val intent = Intent(this, SplashActivity::class.java)
					intent.putExtra("export", data.data!!)
					startActivity(intent)
					finish()
				}

			intentReference === IntentReference.IMPORT_FILE ->
				requireActivity().apply {
					val intent = Intent(this, SplashActivity::class.java)
					intent.putExtra("import", data.data!!)
					startActivity(intent)
					finish()
				}
		}
	}

	override fun onResume() {
		super.onResume()
		trainingFloatingButton?.updateFloatingActionButton()
	}
}