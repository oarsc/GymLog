package org.scp.gymlog.ui.main.muscles

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import org.scp.gymlog.R
import org.scp.gymlog.SplashActivity
import org.scp.gymlog.databinding.ListitemMuscleBinding
import org.scp.gymlog.model.Muscle
import org.scp.gymlog.room.entities.GymEntity
import org.scp.gymlog.service.DataBaseDumperService
import org.scp.gymlog.service.NotificationService
import org.scp.gymlog.ui.common.CustomFragment
import org.scp.gymlog.ui.common.components.TrainingFloatingActionButton
import org.scp.gymlog.ui.common.components.listView.CommonListView
import org.scp.gymlog.ui.common.components.listView.SimpleListHandler
import org.scp.gymlog.ui.common.components.listView.SimpleListView
import org.scp.gymlog.ui.common.dialogs.EditTextDialogFragment
import org.scp.gymlog.ui.common.dialogs.MenuDialogFragment.Companion.DIALOG_CLOSED
import org.scp.gymlog.ui.common.dialogs.TextSelectDialogFragment
import org.scp.gymlog.ui.create.CreateExerciseActivity
import org.scp.gymlog.ui.exercises.ExercisesActivity
import org.scp.gymlog.ui.exercises.LatestActivity
import org.scp.gymlog.ui.exercises.SearchActivity
import org.scp.gymlog.ui.preferences.PreferencesDefinition
import org.scp.gymlog.util.Constants.IntentReference
import org.scp.gymlog.util.Data
import org.scp.gymlog.util.DateUtils.currentDateTime
import org.scp.gymlog.util.extensions.DatabaseExts.dbThread
import org.scp.gymlog.util.extensions.PreferencesExts.save


/**
 * A fragment representing a list of Items.
 */
class MusclesFragment : CustomFragment() {
/**
 * Mandatory empty constructor for the fragment manager to instantiate the
 * fragment (e.g. upon screen orientation changes).
 */
	private var trainingFloatingButton: TrainingFloatingActionButton? = null

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		val view = inflater.inflate(R.layout.fragment_list_muscles, container, false)

		view.findViewById<SimpleListView<Muscle, ListitemMuscleBinding>>(R.id.musclesList)
			.init(Data.muscles, MuscleListHandler())

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
					val intent = Intent(context, SearchActivity::class.java)
					startActivity(intent, IntentReference.SEARCH_LIST)
				}
				R.id.latestButton -> {
					requireActivity().apply {
						val intent = Intent(this, LatestActivity::class.java)
						startActivity(intent)
					}
				}
				R.id.gymSelectButton -> {
					val context = requireContext()

					context.dbThread { db ->
						val gyms = db.gymDao().getAll()
						val labels = gyms
							.map(GymEntity::name)
							.toMutableList()
							.apply { add("Add new gym") }
						val dialog = TextSelectDialogFragment(labels, Data.currentGym - 1) { idx, _ ->
							if (idx != DIALOG_CLOSED) {
								if (idx == gyms.size) {
									val dialog = EditTextDialogFragment(R.string.dialog_write_new_gym, confirm = {
										context.dbThread { db ->
											db.gymDao().insert(GymEntity(name = it))
											val gymId = idx + 1;
											context.save(PreferencesDefinition.CURRENT_GYM, gymId)
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
									context.save(PreferencesDefinition.CURRENT_GYM, gymId)
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
					val seconds = 10
					val date = currentDateTime().plusSeconds(seconds.toLong())
					NotificationService(requireContext())
						.startNewNotification(date, seconds, Data.exercises[0].variations[0])
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

	inner class MuscleListHandler : SimpleListHandler<Muscle, ListitemMuscleBinding> {
		override val useListState = false
		override val itemInflater: (LayoutInflater, ViewGroup?, Boolean) -> ListitemMuscleBinding
			= ListitemMuscleBinding::inflate

		override fun buildListView(
			binding: ListitemMuscleBinding,
			item: Muscle,
			index: Int,
			state: CommonListView.ListElementState?
		) {
			binding.apply {
				root.setOnClickListener { onMuscleClicked(item) }
				content.setText(item.text)
				image.setImageResource(item.icon)
				image.imageTintList = ColorStateList.valueOf(
					resources.getColor(item.color, null)
				)
				indicator.setBackgroundResource(item.color)
			}
		}
	}
}