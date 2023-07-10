package org.scp.gymlog.ui.exercises

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.scp.gymlog.R
import org.scp.gymlog.exceptions.InternalException
import org.scp.gymlog.model.Muscle
import org.scp.gymlog.model.Order
import org.scp.gymlog.model.Order.Companion.getByCode
import org.scp.gymlog.model.Variation
import org.scp.gymlog.room.AppDatabase
import org.scp.gymlog.room.DBThread
import org.scp.gymlog.ui.common.DBAppCompatActivity
import org.scp.gymlog.ui.common.components.TrainingFloatingActionButton
import org.scp.gymlog.ui.common.dialogs.TextDialogFragment
import org.scp.gymlog.ui.create.CreateExerciseActivity
import org.scp.gymlog.ui.registry.RegistryActivity
import org.scp.gymlog.ui.top.TopActivity
import org.scp.gymlog.util.Constants.IntentReference
import org.scp.gymlog.util.Data
import java.util.function.Consumer

class ExercisesActivity : DBAppCompatActivity() {

    private var muscleId = 0
    private lateinit var exercisesId: MutableList<Int>
    private var order: Order = Order.ALPHABETICALLY
    private lateinit var recyclerAdapter: ExercisesRecyclerViewAdapter
    private lateinit var exercisesRecyclerView: RecyclerView

    override fun onLoad(savedInstanceState: Bundle?, db: AppDatabase): Int {
        muscleId = intent.extras!!.getInt("muscleId")
        exercisesId = db.exerciseDao().getExercisesIdByMuscleId(muscleId).toMutableList()
        return CONTINUE
    }

    override fun onDelayedCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_exercises)
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        order = getByCode(
            preferences.getString("exercisesSortLastUsed", Order.ALPHABETICALLY.code)!!)

        val muscle = Data.muscles
            .filter { it.id == muscleId }
            .getOrElse(0) { throw InternalException("Muscle id not found") }

        setTitle(muscle.text)

        recyclerAdapter = ExercisesRecyclerViewAdapter(exercisesId, order) { variation, action ->
            onExerciseItemMenuSelected(variation, action) }

        recyclerAdapter.onClickListener = Consumer { variation ->
            val intent = Intent(this, RegistryActivity::class.java)
            intent.putExtra("exerciseId", variation.exercise.id)
            intent.putExtra("variationId", variation.id)
            startActivityForResult(intent, IntentReference.REGISTRY)
        }

        exercisesRecyclerView = findViewById<RecyclerView>(R.id.exercisesList).apply {
            layoutManager = LinearLayoutManager(this@ExercisesActivity)
            adapter = recyclerAdapter
        }

        findViewById<TrainingFloatingActionButton>(R.id.fabTraining).updateFloatingActionButton()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.exercises_menu, menu)

        when (order) {
            Order.ALPHABETICALLY -> menu.findItem(R.id.sortAlphabetically).isChecked = true
            Order.LAST_USED -> menu.findItem(R.id.sortLastUsed).isChecked = true
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.create_button -> {
                val intent = Intent(this, CreateExerciseActivity::class.java)
                intent.putExtra("muscleId", muscleId)
                startActivityForResult(intent, IntentReference.CREATE_EXERCISE_FROM_MUSCLE)
            }
            R.id.sortAlphabetically -> {
                order = Order.ALPHABETICALLY.also { recyclerAdapter.switchOrder(it) }
                val editor = PreferenceManager.getDefaultSharedPreferences(this).edit()
                editor.putString("exercisesSortLastUsed", order.code)
                editor.apply()
                item.isChecked = true
            }
            R.id.sortLastUsed -> {
                order = Order.LAST_USED.also { recyclerAdapter.switchOrder(it) }
                val editor = PreferenceManager.getDefaultSharedPreferences(this).edit()
                editor.putString("exercisesSortLastUsed", order.code)
                editor.apply()
                item.isChecked = true
            }
        }
        return false
    }

    private fun onExerciseItemMenuSelected(variation: Variation, action: Int) {
        val exercise = variation.exercise
        when (action) {
            R.id.topRanking -> {
                val intent = Intent(this, TopActivity::class.java)
                intent.putExtra("exerciseId", exercise.id)
                startActivity(intent)
            }
            R.id.editExercise -> {
                val intent = Intent(this, CreateExerciseActivity::class.java)
                intent.putExtra("exerciseId", exercise.id)
                startActivityForResult(intent, IntentReference.EDIT_EXERCISE)
            }
            R.id.removeExercise -> {
                val dialog = TextDialogFragment(
                    R.string.dialog_confirm_remove_exercise_title,
                    R.string.dialog_confirm_remove_exercise_text
                ) { confirmed ->
                    if (confirmed) {
                        DBThread.run(this) { db ->
                            if (db.exerciseDao().delete(exercise.toEntity()) == 1) {
                                runOnUiThread { recyclerAdapter.removeExercise(exercise) }
                                db.trainingDao().deleteEmptyTraining()

                                exercisesId.remove(variation.id)
                                Data.exercises.removeIf { it === exercise }
                            }
                        }
                    }
                }
                dialog.show(supportFragmentManager, null)
            }
        }
    }

    override fun onActivityResult(intentReference: IntentReference, data: Intent) {
        when (intentReference) {
            IntentReference.EDIT_EXERCISE -> {
                val exerciseId = data.getIntExtra("exerciseId", -1)
                val exercise = Data.getExercise(exerciseId)
                val hasMuscle = exercise.primaryMuscles
                    .any { it.id == muscleId }

                if (hasMuscle) {
                    recyclerAdapter.updateNotify(exercise)
                } else {
                    recyclerAdapter.removeExercise(exercise)
                    exercisesId.remove(exercise.id)
                }
            }
            IntentReference.CREATE_EXERCISE_FROM_MUSCLE -> {
                val exerciseId = data.getIntExtra("exerciseId", -1)
                val exercise = Data.getExercise(exerciseId)
                val hasMuscle = exercise.primaryMuscles
                    .any { it.id == muscleId }

                if (hasMuscle) {
                    exercisesId.add(exercise.id)
                    recyclerAdapter.addExercise(exercise)
                }
            }
            IntentReference.REGISTRY -> {
                if (data.getBooleanExtra("refresh", false)) {
                    if (order == Order.ALPHABETICALLY) {
                        val id = data.getIntExtra("exerciseId", -1)
                        val ex = Data.getExercise(id)
                        recyclerAdapter.updateNotify(ex)
                    } else {
//                        if (modified)
//                            exercisesRecyclerView.scrollToPosition(0)
                        recyclerAdapter.switchOrder(order)
                    }
                }
            }
            else -> {}
        }
    }
}