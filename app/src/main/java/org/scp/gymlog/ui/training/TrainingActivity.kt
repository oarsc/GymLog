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
import org.scp.gymlog.room.AppDatabase
import org.scp.gymlog.ui.common.DBAppCompatActivity
import org.scp.gymlog.ui.common.components.listView.MultipleListView
import org.scp.gymlog.ui.main.history.HistoryFragment.Companion.getTrainingData
import org.scp.gymlog.ui.main.history.TrainingData
import org.scp.gymlog.ui.preferences.PreferencesDefinition
import org.scp.gymlog.ui.training.rows.TrainingRowData
import org.scp.gymlog.util.DateUtils.getDateString
import org.scp.gymlog.util.extensions.PreferencesExts.loadBoolean

class TrainingActivity : DBAppCompatActivity() {

    private val rowsData = mutableListOf<TrainingRowData>()
    private lateinit var trainingData: TrainingData

    private lateinit var trainingListView: MultipleListView<TrainingRowData>
    private lateinit var trainingListHandler: TrainingListHandler


    private var trainingId: Int = 0

    override fun onLoad(savedInstanceState: Bundle?, db: AppDatabase): Int {
        trainingId = intent.extras!!.getInt("trainingId")
        val training = db.trainingDao().getTraining(trainingId)
            ?: throw LoadException("Cannot find trainingId: $trainingId")
        val bits = db.bitDao().getHistoryByTrainingId(trainingId)
        trainingData = getTrainingData(training, bits)

        generateTrainingBitRows(bits.map { Bit(it) })
        return CONTINUE
    }

    override fun onDelayedCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_training)
        setTitle(R.string.title_training)

        val internationalSystem = loadBoolean(PreferencesDefinition.UNIT_INTERNATIONAL_SYSTEM)

        setHeaderInfo()

        val focusBitId = intent.extras!!.getInt("focusBit", -1)
            .let { if (it < 0) null else it }

        trainingListHandler = TrainingListHandler(this, internationalSystem, focusBitId)

        trainingListView = findViewById(R.id.historyList)
        trainingListView.init(rowsData, trainingListHandler)


        focusBitId?.let { bitId ->
            rowsData.withIndex()
                .firstOrNull { it.value.any { bit -> bit.id == bitId } }
                ?.let { trainingListView.scrollToPosition(it.index, 60) }
        }

        trainingListHandler.setOnBitChangedListener {
            Intent().also {
                it.putExtra("refresh", true)
                setResult(RESULT_OK, it)
            }
        }

    }

    private fun generateTrainingBitRows(bits: List<Bit>) {
        rowsData.clear()

        for (bit in bits) {
            val variation = bit.variation

            val currentRowData =
                if (bit.superSet > 0) {
                    rowsData
                        .firstOrNull { it.superSet == bit.superSet }
                        ?.also { it.addVariation(variation) }
                        ?: TrainingRowData(variation, bit.superSet).also { rowsData.add(it) }

                } else {
                    rowsData.lastOrNull()
                        ?.let { if (it.superSet == null && it.variation === variation) it else null }
                        ?: TrainingRowData(variation).also { rowsData.add(it) }
                }

            currentRowData.add(bit)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.expand_collapse_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == R.id.expandButton) {
            trainingListView.applyToAll { binding, item, _ ->
                trainingListHandler.expandItem(binding, item)
            }

        } else if (menuItem.itemId == R.id.collapseButton) {
            trainingListView.applyToAll { binding, item, _ ->
                trainingListHandler.collapseItem(binding, item)
            }
            trainingListView.notifyDataSetChanged()
            trainingListView.scrollToPosition(0)
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