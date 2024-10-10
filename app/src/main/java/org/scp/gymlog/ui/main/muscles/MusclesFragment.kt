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
import org.scp.gymlog.model.Gym
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
import org.scp.gymlog.ui.exercises.SearchActivity
import org.scp.gymlog.ui.preferences.PreferencesDefinition.CURRENT_GYM
import org.scp.gymlog.ui.preferences.PreferencesDefinition.DROPBOX_CREDENTIAL
import org.scp.gymlog.util.Constants.IntentReference
import org.scp.gymlog.util.Data
import org.scp.gymlog.util.DateUtils.NOW
import org.scp.gymlog.util.extensions.DatabaseExts.dbThread
import org.scp.gymlog.util.extensions.MessagingExts.toast
import org.scp.gymlog.util.extensions.PreferencesExts.loadDbxCredential
import org.scp.gymlog.util.extensions.PreferencesExts.save
import org.scp.gymlog.util.extensions.RedirectionExts.goToVariation


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
				R.id.gymSelectButton -> {
					val context = requireContext()

					val labels = Data.gyms
						.map(Gym::name)
						.toMutableList()
						.apply { add("Add new gym") }

					val currentGymId = Data.gym?.id ?: 0
					val dialog = TextSelectDialogFragment(labels, currentGymId - 1) { idx, _ ->
						if (idx != DIALOG_CLOSED) {
							if (idx == Data.gyms.size) {
								val dialog = EditTextDialogFragment(R.string.dialog_write_new_gym, confirm = {
									context.dbThread { db ->
										db.gymDao().insert(GymEntity(name = it))
										val gymId = idx + 1;
										context.save(CURRENT_GYM, gymId)
										Data.gym = Data.getGym(gymId)

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
								context.save(CURRENT_GYM, gymId)
								Data.gym = Data.getGym(gymId)

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
				R.id.importButton -> {
					val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
					intent.addCategory(Intent.CATEGORY_OPENABLE)
					intent.type = "application/json"
					intent.putExtra(Intent.EXTRA_TITLE, DataBaseDumperService.OUTPUT)
					startActivityForResult(intent, IntentReference.IMPORT_FILE)
				}
				R.id.exportButton -> {
					val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
					intent.addCategory(Intent.CATEGORY_OPENABLE)
					intent.type = "application/json"
					intent.putExtra(Intent.EXTRA_TITLE, DataBaseDumperService.OUTPUT)
					startActivityForResult(intent, IntentReference.EXPORT_FILE)
				}
				R.id.dropboxExportButton -> {
					requireActivity().apply {
						val intent = Intent(this, SplashActivity::class.java)
						intent.action = "dropbox"
						startActivity(intent)
						finish()
					}
				}
				R.id.dropboxRevokeButton -> {
					context?.apply {
						loadDbxCredential(DROPBOX_CREDENTIAL)
							?.also {
								save(DROPBOX_CREDENTIAL, null)
								toast("Cleaning dropbox credentials")
							} ?: toast("Nothing to do")
					}
				}

				R.id.testButton -> {
					val seconds = 10
					val date = NOW.plusSeconds(seconds.toLong())
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