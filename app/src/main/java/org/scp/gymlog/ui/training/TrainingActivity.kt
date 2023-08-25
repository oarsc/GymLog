package org.scp.gymlog.ui.training

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import org.scp.gymlog.R
import org.scp.gymlog.exceptions.LoadException
import org.scp.gymlog.model.Bit
import org.scp.gymlog.model.Muscle
import org.scp.gymlog.model.TrainingOrder
import org.scp.gymlog.room.AppDatabase
import org.scp.gymlog.room.entities.BitEntity
import org.scp.gymlog.ui.common.DBAppCompatActivity
import org.scp.gymlog.ui.common.components.listView.MultipleListView
import org.scp.gymlog.ui.main.history.HistoryFragment.Companion.getTrainingData
import org.scp.gymlog.ui.main.history.TrainingData
import org.scp.gymlog.ui.training.rows.TrainingBitRow
import org.scp.gymlog.ui.training.rows.TrainingHeaderRow
import org.scp.gymlog.ui.training.rows.TrainingVariationRow
import org.scp.gymlog.util.Data
import org.scp.gymlog.util.DateUtils.getDateString
import org.scp.gymlog.util.extensions.PreferencesExts.loadBoolean
import org.scp.gymlog.util.extensions.PreferencesExts.loadString

class TrainingActivity : DBAppCompatActivity() {

    private val exerciseRows = mutableListOf<ExerciseRows>()
    private lateinit var trainingData: TrainingData

    private lateinit var exercisesListView: MultipleListView<ExerciseRows>
    private lateinit var exercisesListHandler: TrainingListHandler


    private var trainingId: Int = 0

    override fun onLoad(savedInstanceState: Bundle?, db: AppDatabase): Int {
        trainingId = intent.extras!!.getInt("trainingId")
        val training = db.trainingDao().getTraining(trainingId)
            ?: throw LoadException("Cannot find trainingId: $trainingId")
        val bits = db.bitDao().getHistoryByTrainingId(trainingId)
        trainingData = getTrainingData(training, bits)
        generateTrainingBitRows(bits)
        return CONTINUE
    }

    override fun onDelayedCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_training)
        setTitle(R.string.title_training)

        val internationalSystem = loadBoolean("internationalSystem", true)

        setHeaderInfo()

        val focusBit = intent.extras!!.getInt("focusBit", -1)
        val focusElement = if (focusBit < 0) -1 else {
            exerciseRows.withIndex()
                .firstOrNull {
                    it.value
                        .filterIsInstance<TrainingBitRow>()
                        .any { row -> row.bit.id == focusBit }
                }
                ?.index ?: -1
        }

        exercisesListHandler = TrainingListHandler(this, internationalSystem, focusElement)

        exercisesListView = findViewById(R.id.historyList)
        exercisesListView.init(exerciseRows, exercisesListHandler)

        if (focusElement >= 0) {
            exercisesListView.scrollToPosition(focusElement, 60)
        }

        exercisesListHandler.setOnBitChangedListener {
            Intent().also {
                it.putExtra("refresh", true)
                setResult(RESULT_OK, it)
            }
        }

    }

    private fun generateTrainingBitRows(bits: List<BitEntity>) {
        exerciseRows.clear()

        val order = TrainingOrder.getByCode(
            loadString("trainingBitsSort", TrainingOrder.CHRONOLOGICALLY.code)
        )

        for (bit in bits) {
            val variation = Data.getVariation(bit.variationId)
            val exercise = variation.exercise

            val exerciseRow =
                if (bit.superSet > 0) {
                    exerciseRows
                        .firstOrNull { it.superSet == bit.superSet }
                        ?.also { it.addVariation(variation) }
                        ?: ExerciseRows(variation, bit.superSet).also { exerciseRows.add(it) }

                } else if (order == TrainingOrder.CHRONOLOGICALLY) {
                    exerciseRows.lastOrNull()
                        ?.let { if (it.superSet == null && it.exercise === exercise) it else null }
                        ?: ExerciseRows(variation).also { exerciseRows.add(it) }

                } else {
                    exerciseRows
                        .filter { it.superSet == null && it.exercise === exercise }
                        .getOrElse(0) {
                            ExerciseRows(variation).also { exerciseRows.add(it) }
                        }
                }

            val lastVariationId = getLastVar(exerciseRow)
            val isSuperSet = exerciseRow.superSet != null

            if (isSuperSet) {
                if (exerciseRow.isEmpty()) {
                    exerciseRow.add(TrainingHeaderRow(true))
                }
            } else if (variation.id != lastVariationId) {
                if (!variation.default) {
                    exerciseRow.add(TrainingVariationRow(variation))
                    exerciseRow.add(TrainingHeaderRow())
                } else if (lastVariationId > 0) {
                    exerciseRow.add(TrainingVariationRow(variation))
                    exerciseRow.add(TrainingHeaderRow())
                } else if (exerciseRow.isEmpty()) {
                    exerciseRow.add(TrainingHeaderRow())
                }
            }
            exerciseRow.add(TrainingBitRow(Bit(bit)))
        }
    }

    private fun getLastVar(exerciseRow: ExerciseRows): Int {
        return exerciseRow
            .reversed()
            .filterIsInstance<TrainingVariationRow>()
            .map { it.variation.id }
            .getOrElse(0) { 0 }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.expand_collapse_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == R.id.expandButton) {
            exercisesListView.applyToAll { binding, item, state ->
                exercisesListHandler.expandAll(binding, item, state!!)
            }

        } else if (menuItem.itemId == R.id.collapseButton) {
            exercisesListView.applyToAll { binding, item, state ->
                exercisesListHandler.collapseAll(binding, item, state!!)
            }
            exercisesListView.notifyDataSetChanged()
            exercisesListView.scrollToPosition(0)
        }
        return false
    }

    @SuppressLint("SetTextI18n")
    private fun setHeaderInfo() {
        val fragment = findViewById<View>(R.id.fragmentTraining)
        val title = findViewById<TextView>(R.id.title)
        val subtitle = findViewById<TextView>(R.id.subtitle)
        val indicator = findViewById<View>(R.id.indicator)

        fragment.isClickable = false

        title.text = String.format(
            resources.getString(R.string.compound_training_date),
            trainingData.id,
            trainingData.startTime.getDateString()
        )

        subtitle.text = trainingData.mostUsedMuscles
            .map(Muscle::text)
            .map { textRes -> resources.getString(textRes) }
            .joinToString { it }

        indicator.setBackgroundResource(trainingData.mostUsedMuscles[0].color)
    }
}