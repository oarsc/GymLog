package org.scp.gymlog.ui.exercises

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.SearchView
import org.scp.gymlog.R
import org.scp.gymlog.databinding.ListitemExercisesRowBinding
import org.scp.gymlog.model.Exercise
import org.scp.gymlog.model.Variation
import org.scp.gymlog.ui.common.CustomAppCompatActivity
import org.scp.gymlog.ui.common.components.listView.SimpleListView
import org.scp.gymlog.ui.common.dialogs.MenuDialogFragment
import org.scp.gymlog.ui.common.dialogs.TextDialogFragment
import org.scp.gymlog.ui.create.CreateExerciseActivity
import org.scp.gymlog.ui.top.TopActivity
import org.scp.gymlog.util.Constants.IntentReference
import org.scp.gymlog.util.Data
import org.scp.gymlog.util.extensions.DatabaseExts.dbThread
import org.scp.gymlog.util.extensions.RedirectionExts.goToVariation
import java.util.*


class SearchActivity : CustomAppCompatActivity() {

    private lateinit var exercisesListView: SimpleListView<Exercise, ListitemExercisesRowBinding>
    private lateinit var handler: ExercisesListHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_search)
        setTitle(R.string.symbol_empty)

        exercisesListView = findViewById(R.id.exercisesList)

        handler = ExercisesListHandler(this, exercisesListView)
        exercisesListView.init(Data.exercises, handler)
        exercisesListView.sort(Comparator.comparing { it.name.lowercase(Locale.getDefault()) })
        handler.onExerciseClicked(this::itemClicked)
        handler.onVariationClicked(this::itemClicked)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.search_menu, menu)

        val menuItem = menu.findItem(R.id.searchButton)
        val searchView = menuItem.actionView as SearchView

        menuItem.expandActionView()
        menuItem.setOnActionExpandListener(object: MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?) = true
            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
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

                exercisesListView.scrollToPosition(0)

                val exes = Data.exercises.filter {
                    val exerciseName = it.name.lowercase(Locale.getDefault())
                    textParts.all { part -> exerciseName.contains(part) }
                }

                exercisesListView.setListData(exes)
                exercisesListView.notifyDataSetChanged()
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
            goToVariation(variation)
        }
    }

    private fun itemClicked(variation: Variation) {
        goToVariation(variation)
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
                                runOnUiThread { exercisesListView.remove(exercise) }
                                db.trainingDao().deleteEmptyTraining()
                                Data.exercises.removeIf { it === exercise }
                            }
                        }
                    }
                }
                dialog.show(supportFragmentManager, null)
            }
        }
    }
}