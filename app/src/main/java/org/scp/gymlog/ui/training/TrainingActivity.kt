package org.scp.gymlog.ui.training

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.scp.gymlog.R
import org.scp.gymlog.exceptions.LoadException
import org.scp.gymlog.model.Bit
import org.scp.gymlog.model.Muscle
import org.scp.gymlog.model.Order
import org.scp.gymlog.model.TrainingOrder
import org.scp.gymlog.room.AppDatabase
import org.scp.gymlog.ui.common.DBAppCompatActivity
import org.scp.gymlog.ui.main.history.HistoryFragment.Companion.getTrainingData
import org.scp.gymlog.ui.main.history.TrainingData
import org.scp.gymlog.ui.training.rows.ITrainingRow
import org.scp.gymlog.ui.training.rows.TrainingBitRow
import org.scp.gymlog.ui.training.rows.TrainingHeaderRow
import org.scp.gymlog.ui.training.rows.TrainingVariationRow
import org.scp.gymlog.util.Data
import org.scp.gymlog.util.DateUtils.getDateString
import java.util.function.Consumer

class TrainingActivity : DBAppCompatActivity() {

    private val exerciseRows = mutableListOf<ExerciseRows>()
    private lateinit var trainingData: TrainingData
    private lateinit var adapter: TrainingMainRecyclerViewAdapter
    private lateinit var linearLayout: LinearLayoutManager

    override fun onLoad(savedInstanceState: Bundle?, db: AppDatabase): Int {
        val trainingId = intent.extras!!.getInt("trainingId")
        val training = db.trainingDao().getTraining(trainingId)
            ?: throw LoadException("Cannot find trainingId: $trainingId")
        val bits = db.bitDao().getHistoryByTrainingId(trainingId)
        trainingData = getTrainingData(training, bits)

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val order = TrainingOrder.getByCode(
            preferences.getString("trainingBitsSort", TrainingOrder.CHRONOLOGICALLY.code)!!)

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
        return CONTINUE
    }

    override fun onDelayedCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_training)
        setTitle(R.string.title_training)

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val internationalSystem = preferences.getBoolean("internationalSystem", true)

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

        val historyRecyclerView: RecyclerView = findViewById(R.id.historyList)
        historyRecyclerView.layoutManager = LinearLayoutManager(this).also { linearLayout = it }
        historyRecyclerView.adapter = TrainingMainRecyclerViewAdapter(exerciseRows,
            internationalSystem, focusElement
        ).also { adapter = it }

        if (focusElement >= 0) {
            linearLayout.scrollToPositionWithOffset(focusElement, 60)
        }

        adapter.onBitChangedListener = Consumer {
            val data = Intent()
            data.putExtra("refresh", true)
            setResult(RESULT_OK, data)
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.expandButton) {
            adapter.expandAll()
        } else if (item.itemId == R.id.collapseButton) {
            adapter.collapseAll()
            linearLayout.scrollToPosition(0)
        }
        return false
    }

    @SuppressLint("SetTextI18n")
    private fun setHeaderInfo() {
        val fragment: View = findViewById(R.id.fragmentTraining)
        val title: TextView = findViewById(R.id.title)
        val subtitle: TextView = findViewById(R.id.subtitle)
        val indicator: View = findViewById(R.id.indicator)

        fragment.isClickable = false

        title.text = (resources.getString(R.string.text_training) + " #${trainingData.id} " + resources.getString(
                R.string.text_on_smallcaps
            )
                    + " " + trainingData.startDate.getDateString())

        subtitle.text = trainingData.mostUsedMuscles
            .map(Muscle::text)
            .map { textRes -> resources.getString(textRes) }
            .joinToString { it }

        indicator.setBackgroundResource(trainingData.mostUsedMuscles[0].color)
    }
}