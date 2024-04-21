package org.scp.gymlog.ui.training

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import org.scp.gymlog.R
import org.scp.gymlog.exceptions.LoadException
import org.scp.gymlog.model.Bit
import org.scp.gymlog.model.GymRelation
import org.scp.gymlog.model.Muscle
import org.scp.gymlog.model.Training
import org.scp.gymlog.room.AppDatabase
import org.scp.gymlog.ui.common.DBAppCompatActivity
import org.scp.gymlog.ui.common.animations.ResizeHeightAnimation
import org.scp.gymlog.ui.common.components.listView.MultipleListView
import org.scp.gymlog.ui.common.dialogs.EditTrainingDialogFragment
import org.scp.gymlog.ui.main.history.HistoryFragment.Companion.getTrainingData
import org.scp.gymlog.ui.main.history.TrainingData
import org.scp.gymlog.ui.preferences.PreferencesDefinition
import org.scp.gymlog.ui.training.rows.TrainingRowData
import org.scp.gymlog.util.Data
import org.scp.gymlog.util.DateUtils.getDateString
import org.scp.gymlog.util.DateUtils.getTimeString
import org.scp.gymlog.util.DateUtils.minutesToTimeString
import org.scp.gymlog.util.extensions.DatabaseExts.dbThread
import org.scp.gymlog.util.extensions.PreferencesExts.loadBoolean
import java.math.BigDecimal
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class TrainingActivity : DBAppCompatActivity() {

    private val rowsData = mutableListOf<TrainingRowData>()
    private lateinit var trainingData: TrainingData

    private lateinit var trainingListView: MultipleListView<TrainingRowData>
    private lateinit var trainingListHandler: TrainingListHandler

    private var trainingId: Int = 0
    private var canEditGymId = false

    private var canBeReopened = false

    override fun onLoad(savedInstanceState: Bundle?, db: AppDatabase): Int {
        val trainingDao = db.trainingDao()

        trainingId = intent.extras!!.getInt("trainingId")
        val training = trainingDao.getTraining(trainingId)
            ?: throw LoadException("Cannot find trainingId: $trainingId")
        val bitEntities = db.bitDao().getHistoryByTrainingId(trainingId)
        trainingData = getTrainingData(training, bitEntities)

        val bits = bitEntities.map { Bit(it) }
        generateTrainingBitRows(bits)

        canEditGymId = bits.map { it.variation }
            .distinct()
            .all { it.gymRelation != GymRelation.STRICT_RELATION }

        canBeReopened = Data.training == null &&
            training.start.toLocalDate() == LocalDate.now() &&
            (trainingDao.getMaxTrainingId() ?: 0) == trainingId

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
        inflater.inflate(R.menu.training_menu, menu)

        val allTotals = rowsData
            .flatMap { it.superSet?.run { it.variations } ?: listOf(it.variation) }
            .map { it.weightSpec }
            .all { it.weightAffectation == BigDecimal.ONE }

        if (allTotals) {
            val item = menu.findItem(R.id.totalsButton)
            item.isChecked = true
            item.isEnabled = false
        }
        return true
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.expandButton -> {
                trainingListView.applyToAll { binding, item, _ ->
                    trainingListHandler.expandItem(binding, item)
                }

            }
            R.id.collapseButton -> {
                trainingListView.applyToAll { binding, item, _ ->
                    trainingListHandler.collapseItem(binding, item)
                }
                trainingListView.notifyDataSetChanged()
                trainingListView.scrollToPosition(0)
            }
            R.id.totalsButton -> {
                menuItem.isChecked = !menuItem.isChecked
                trainingListHandler.showTotals = menuItem.isChecked
                trainingListView.notifyDataSetChanged()
            }
        }
        return false
    }

    @SuppressLint("SetTextI18n")
    private fun setHeaderInfo() {
        val trainingDetails = findViewById<View>(R.id.trainingDetails)
        val fragment = findViewById<View>(R.id.fragmentTraining)
        val title = findViewById<TextView>(R.id.title)
        val subtitle = findViewById<TextView>(R.id.subtitle)
        val indicator = findViewById<View>(R.id.indicator)
        val gymName = findViewById<TextView>(R.id.gymName)
        val time = findViewById<TextView>(R.id.time)
        val note = findViewById<TextView>(R.id.note)
        val noteRow = findViewById<View>(R.id.noteRow)
        val editTraining = findViewById<ImageView>(R.id.editTraining)
        val reopenTraining = findViewById<ImageView>(R.id.reopenTraining)
        val notesImage = findViewById<ImageView>(R.id.notesImage)

        val training = trainingData.training

        gymName.text = training.gym.name

        val endTime = training.end
        if (endTime == null) {
            time.text = String.format(
                resources.getString(R.string.compound_training_times_ongoing),
                training.start.getTimeString()
            )
        } else {
            val duration = ChronoUnit.MINUTES.between(training.start, endTime)
                .toInt()
                .minutesToTimeString()

            time.text = String.format(
                resources.getString(R.string.compound_training_times),
                training.start.getTimeString(),
                endTime.getTimeString(),
                duration
            )
        }

        if (training.note.isEmpty()) {
            noteRow.visibility = View.GONE
        } else {
            note.text = training.note
            notesImage.visibility = View.VISIBLE
        }

        title.text = String.format(
            resources.getString(R.string.compound_training_date),
            training.id,
            training.start.getDateString()
        )

        subtitle.text = trainingData.mostUsedMuscles
            .map(Muscle::text)
            .map { textRes -> resources.getString(textRes) }
            .joinToString { it }

        indicator.setBackgroundResource(trainingData.mostUsedMuscles[0].color)

        trainingDetails.visibility = View.GONE
        fragment.setOnClickListener {
            val anim = if (trainingDetails.visibility == View.VISIBLE) {
                ResizeHeightAnimation(trainingDetails, 0)
            } else {
                ResizeHeightAnimation(trainingDetails)
            }
            trainingDetails.startAnimation(anim)
        }

        editTraining.setOnClickListener {
            val dialog = EditTrainingDialogFragment(
                R.string.form_edit_training,
                training,
                canEditGymId,
                { result ->
                    gymName.text = result.gym.name

                    val noteRowVisibility = noteRow.visibility
                    if (result.note.isEmpty()) {
                        noteRow.visibility = View.GONE
                        notesImage.visibility = View.GONE
                    } else {
                        noteRow.visibility = View.VISIBLE
                        notesImage.visibility = View.VISIBLE
                        note.text = result.note
                    }

                    if (noteRowVisibility != noteRow.visibility) {
                        trainingDetails.startAnimation(ResizeHeightAnimation(trainingDetails))
                        Intent().apply {
                            putExtra("refresh", true)
                            setResult(RESULT_OK, this)
                        }
                    }

                    dbThread { db ->
                        db.trainingDao().update(result.toEntity())
                    }
                }
            )
            dialog.show(supportFragmentManager, null)
        }

        if (canBeReopened) {
            reopenTraining.setOnClickListener {
                Data.training?.run { return@setOnClickListener }

                dbThread { db ->
                    val trainingEntity = db.trainingDao().getTraining(trainingId)
                        ?: throw LoadException("Can't find trainingId $trainingId")
                    trainingEntity.end = null
                    db.trainingDao().update(trainingEntity)
                    Data.training = Training(trainingEntity)
                }

                time.text = String.format(
                    resources.getString(R.string.compound_training_times_ongoing),
                    training.start.getTimeString()
                )
                reopenTraining.visibility = View.GONE
            }
        } else {
            reopenTraining.visibility = View.GONE
        }

    }
}