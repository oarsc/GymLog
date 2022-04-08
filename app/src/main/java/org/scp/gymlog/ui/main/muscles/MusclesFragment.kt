package org.scp.gymlog.ui.main.muscles

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONException
import org.scp.gymlog.R
import org.scp.gymlog.SplashActivity
import org.scp.gymlog.model.Muscle
import org.scp.gymlog.room.DBThread
import org.scp.gymlog.service.DataBaseDumperService
import org.scp.gymlog.service.NotificationService
import org.scp.gymlog.ui.common.CustomFragment
import org.scp.gymlog.ui.common.components.TrainingFloatingActionButton
import org.scp.gymlog.ui.createexercise.CreateExerciseActivity
import org.scp.gymlog.ui.exercises.ExercisesActivity
import org.scp.gymlog.util.Constants.IntentReference
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

/**
 * A fragment representing a list of Items.
 */
class MusclesFragment : CustomFragment() {
/**
 * Mandatory empty constructor for the fragment manager to instantiate the
 * fragment (e.g. upon screen orientation changes).
 */
	private val dataBaseDumperService = DataBaseDumperService()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		val view = inflater.inflate(R.layout.fragment_list_muscles, container, false)

		val recyclerView: RecyclerView = view.findViewById(R.id.musclesList)
		recyclerView.layoutManager = LinearLayoutManager(context)
		recyclerView.adapter = MusclesRecyclerViewAdapter { muscle -> onMuscleClicked(muscle) }

		view.findViewById<TrainingFloatingActionButton>(R.id.fabTraining)
			.updateFloatingActionButton()

		val toolbar: Toolbar = view.findViewById(R.id.toolbar)
		toolbar.setOnMenuItemClickListener { item: MenuItem ->
			when (item.itemId) {
				R.id.createButton -> {
					val intent = Intent(context, CreateExerciseActivity::class.java)
					startActivity(intent, IntentReference.CREATE_EXERCISE)
					return@setOnMenuItemClickListener true
				}
				R.id.searchButton -> {
				}
				R.id.saveButton -> {
					val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
					intent.addCategory(Intent.CATEGORY_OPENABLE)
					intent.type = "application/json"
					intent.putExtra(Intent.EXTRA_TITLE, DataBaseDumperService.OUTPUT)
					startActivityForResult(intent, IntentReference.SAVE_FILE)
				}
				R.id.loadButton -> {
					val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
					intent.addCategory(Intent.CATEGORY_OPENABLE)
					intent.type = "application/json"
					intent.putExtra(Intent.EXTRA_TITLE, DataBaseDumperService.OUTPUT)
					startActivityForResult(intent, IntentReference.LOAD_FILE)
				}
				R.id.testButton -> {
					val seconds = 10
					val cal = Calendar.getInstance()
					cal.add(Calendar.SECOND, seconds)
					NotificationService(requireContext())
						.showNotification(cal, seconds, "Test notification")
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
		val context = requireContext()
		when {
			intentReference === IntentReference.EXERCISE_LIST -> {
			}
			intentReference === IntentReference.SAVE_FILE -> {
				DBThread.run(context) { db ->
					try {
						(context.contentResolver.openOutputStream(data.data!!) as FileOutputStream)
							.use { fileOutputStream ->
								dataBaseDumperService.save(context, fileOutputStream, db)
								requireActivity().runOnUiThread {
									Toast.makeText(activity, "Saved", Toast.LENGTH_LONG).show()
								}
							}

					} catch (e: JSONException) {
						throw RuntimeException(e)
					} catch (e: IOException) {
						throw RuntimeException(e)
					}
				}
			}
			intentReference === IntentReference.LOAD_FILE -> {
				DBThread.run(context) { db ->
					try {
						context.contentResolver.openInputStream(data.data!!)
							.use { inputStream ->
								dataBaseDumperService.load(context, inputStream!!, db)
								val intent = Intent(activity, SplashActivity::class.java)
								startActivity(intent)
								requireActivity().finish()
							}
					} catch (e: JSONException) {
						throw RuntimeException(e)
					} catch (e: IOException) {
						throw RuntimeException(e)
					}
				}
			}
		}
	}
}