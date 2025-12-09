package org.oar.gymlog.ui.main.muscles

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import org.oar.gymlog.R
import org.oar.gymlog.databinding.FragmentListMusclesBinding
import org.oar.gymlog.databinding.ListitemMuscleBinding
import org.oar.gymlog.model.Gym
import org.oar.gymlog.model.Muscle
import org.oar.gymlog.room.entities.GymEntity
import org.oar.gymlog.service.DataBaseDumperService
import org.oar.gymlog.ui.LoadActivity
import org.oar.gymlog.ui.common.ResultLauncherFragment
import org.oar.gymlog.ui.common.components.TrainingFloatingActionButton
import org.oar.gymlog.ui.common.components.listView.CommonListView
import org.oar.gymlog.ui.common.components.listView.SimpleListHandler
import org.oar.gymlog.ui.common.dialogs.EditTextDialogFragment
import org.oar.gymlog.ui.common.dialogs.MenuDialogFragment.Companion.DIALOG_CLOSED
import org.oar.gymlog.ui.common.dialogs.TextSelectDialogFragment
import org.oar.gymlog.ui.create.CreateExerciseActivity
import org.oar.gymlog.ui.exercises.ExercisesActivity
import org.oar.gymlog.ui.exercises.SearchActivity
import org.oar.gymlog.ui.main.preferences.PreferencesDefinition.CURRENT_GYM
import org.oar.gymlog.util.Constants.IntentReference
import org.oar.gymlog.util.Data
import org.oar.gymlog.util.extensions.DatabaseExts.dbThread
import org.oar.gymlog.util.extensions.MessagingExts.toast
import org.oar.gymlog.util.extensions.PreferencesExts.save
import org.oar.gymlog.util.extensions.RedirectionExts.goToVariation

/**
 * A fragment representing a list of Items.
 */
class MusclesFragment : ResultLauncherFragment() {
/**
 * Mandatory empty constructor for the fragment manager to instantiate the
 * fragment (e.g. upon screen orientation changes).
 */
	private var trainingFloatingButton: TrainingFloatingActionButton? = null

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View = FragmentListMusclesBinding.inflate(inflater, container, false)
        .apply {
			trainingFloatingButton = fabTraining

			musclesList.init(Data.muscles, MuscleListHandler())

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
                                            val gym = GymEntity(name = it)
                                                .apply {
                                                    gymId = db.gymDao().insert(this).toInt()
                                                }
                                                .let(::Gym)

                                            Data.gyms.add(gym)
                                            Data.gym = gym
                                            context.save(CURRENT_GYM, gym.id)

                                            requireActivity().apply {
                                                val intent = Intent(this, LoadActivity::class.java)
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
                                        val intent = Intent(this, LoadActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    }
                                }
                            }
                        }
                        dialog.show(requireActivity().supportFragmentManager, null)
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
                            val intent = Intent(this, LoadActivity::class.java)
                            intent.action = "dropbox"
                            startActivity(intent)
                            finish()
                        }
                    }
                }
                false
            }
        }
        .root

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
					val intent = Intent(this, LoadActivity::class.java)
					intent.putExtra("export", data.data!!)
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