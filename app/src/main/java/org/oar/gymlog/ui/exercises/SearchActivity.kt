package org.oar.gymlog.ui.exercises

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.SearchView
import org.oar.gymlog.R
import org.oar.gymlog.databinding.ActivitySearchBinding
import org.oar.gymlog.databinding.ListitemExercisesRowBinding
import org.oar.gymlog.model.Exercise
import org.oar.gymlog.model.Muscle
import org.oar.gymlog.model.Variation
import org.oar.gymlog.ui.common.BindingAppCompatActivity
import org.oar.gymlog.ui.common.components.listView.SimpleListView
import org.oar.gymlog.ui.common.dialogs.MenuDialogFragment
import org.oar.gymlog.ui.common.dialogs.TextDialogFragment
import org.oar.gymlog.ui.create.CreateExerciseActivity
import org.oar.gymlog.ui.top.TopActivity
import org.oar.gymlog.util.Constants.IntentReference
import org.oar.gymlog.util.Data
import org.oar.gymlog.util.extensions.DatabaseExts.dbThread
import org.oar.gymlog.util.extensions.RedirectionExts.goToVariation
import java.util.Locale


class SearchActivity : BindingAppCompatActivity<ActivitySearchBinding>(ActivitySearchBinding::inflate) {
    private var muscle: Muscle? = null
    private lateinit var exercises: MutableList<Exercise>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.apply {
            setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
            setOnMenuItemClickListener(::onOptionsItemSelected)
            onCreateOptionsMenu(menu)
        }

        muscle = intent.extras?.getInt("muscleId", -1)
            ?.let { if(it < 0) null else it }
            ?.let(Data::getMuscle)

        exercises = muscle
            ?.let { m -> Data.exercises.filter { it.primaryMuscles.contains(m) }.toMutableList()}
            ?: Data.exercises.toMutableList()

        val exercisesList = binding.exercisesList as SimpleListView<Exercise, ListitemExercisesRowBinding>
        val handler = ExercisesListHandler(this, exercisesList, muscle)
        exercisesList.init(exercises, handler)
        exercisesList.sort(Comparator.comparing { it.name.lowercase(Locale.getDefault()) })
        handler.onExerciseClicked(this::itemClicked)
        handler.onVariationClicked(this::itemClicked)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val menuItem = menu.findItem(R.id.searchButton)
        val searchView = menuItem.actionView as SearchView

        menuItem.expandActionView()
        menuItem.setOnActionExpandListener(object: MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem) = true
            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                finish()
                return true
            }
        })

        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchView.clearFocus()
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                val textParts = newText?.lowercase(Locale.getDefault())?.split(' ')
                    ?: return true

                binding.exercisesList.scrollToPosition(0)

                val exes = exercises.filter {
                    val exerciseName = it.name.lowercase(Locale.getDefault())
                    textParts.all { part -> exerciseName.contains(part) }
                }

                binding.exercisesList.setListData(exes)
                binding.exercisesList.notifyDataSetChanged()
                return true
            }
        })
        return true
    }

    private fun itemClicked(exercise: Exercise, long: Boolean) {
        if (long) {
            MenuDialogFragment(R.menu.exercise_menu) { action ->
                exerciseMenuActionSelected(exercise, action)
            }.apply { show(supportFragmentManager, null) }
        } else {
            val variation = exercise.variations.first { it.default }
            goToVariation(variation, muscle)
        }
    }

    private fun itemClicked(variation: Variation) {
        goToVariation(variation, muscle)
    }

    private fun exerciseMenuActionSelected(exercise: Exercise, action: Int) {
        when (action) {
            R.id.open -> {
                itemClicked(exercise, false)
            }
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
                        dbThread { db ->
                            if (db.exerciseDao().delete(exercise.toEntity()) == 1) {
                                runOnUiThread { binding.exercisesList.remove(exercise) }
                                db.trainingDao().deleteEmptyTraining()
                                Data.exercises.removeIf { it === exercise }
                                exercises.removeIf { it === exercise }

                                val data = Intent()
                                data.putExtra("refresh", true)
                                setResult(RESULT_OK, data)
                            }
                        }
                    }
                }
                dialog.show(supportFragmentManager, null)
            }
        }
    }
}