package org.oar.gymlog.ui.training

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.core.view.isVisible
import org.oar.gymlog.R
import org.oar.gymlog.databinding.ActivityTrainingBinding
import org.oar.gymlog.exceptions.LoadException
import org.oar.gymlog.model.Bit
import org.oar.gymlog.model.GymRelation
import org.oar.gymlog.model.Muscle
import org.oar.gymlog.model.Training
import org.oar.gymlog.room.AppDatabase
import org.oar.gymlog.ui.common.DatabaseAppCompatActivity
import org.oar.gymlog.ui.common.animations.ResizeHeightAnimation
import org.oar.gymlog.ui.common.components.listView.MultipleListView
import org.oar.gymlog.ui.common.dialogs.EditTrainingDialogFragment
import org.oar.gymlog.ui.main.history.HistoryFragment.Companion.getTrainingData
import org.oar.gymlog.ui.main.history.TrainingData
import org.oar.gymlog.ui.main.preferences.PreferencesDefinition
import org.oar.gymlog.ui.training.rows.TrainingRowData
import org.oar.gymlog.util.Data
import org.oar.gymlog.util.DateUtils.getDateString
import org.oar.gymlog.util.DateUtils.getTimeString
import org.oar.gymlog.util.DateUtils.minutesToTimeString
import org.oar.gymlog.util.extensions.DatabaseExts.dbThread
import org.oar.gymlog.util.extensions.PreferencesExts.loadBoolean
import java.math.BigDecimal
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class TrainingActivity : DatabaseAppCompatActivity<ActivityTrainingBinding>(ActivityTrainingBinding::inflate) {

    private val rowsData = mutableListOf<TrainingRowData>()
    private lateinit var trainingData: TrainingData

    private lateinit var trainingListHandler: TrainingListHandler

    private var trainingId: Int = 0
    private var canEditGymId = false

    private var canBeReopened = false

    override fun preLoad(savedInstanceState: Bundle?) {
        // Set initial size for hidden views
        binding.trainingDetails.layoutParams.height = 1
    }

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
        val internationalSystem = loadBoolean(PreferencesDefinition.UNIT_INTERNATIONAL_SYSTEM)

        setHeaderInfo()

        // Toolbar
        binding.toolbar.apply {
            setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
            setOnMenuItemClickListener(::onOptionsItemSelected)

            val allTotals = rowsData
                .flatMap { it.superSet?.run { it.variations } ?: listOf(it.variation) }
                .map { it.weightSpec }
                .all { it.weightAffectation == BigDecimal.ONE }

            if (allTotals) {
                val item = menu.findItem(R.id.totalsButton)
                item.isChecked = true
                item.isEnabled = false
            }
        }

        val focusBitId = intent.extras!!.getInt("focusBit", -1)
            .let { if (it < 0) null else it }

        trainingListHandler = TrainingListHandler(this, internationalSystem, focusBitId)

        binding.historyList.init(rowsData, trainingListHandler)

        focusBitId?.let { bitId ->
            rowsData.withIndex()
                .firstOrNull { it.value.any { bit -> bit.id == bitId } }
                ?.let { binding.historyList.scrollToPosition(it.index, 60) }
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

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        val historyList = binding.historyList as MultipleListView<TrainingRowData>

        when (menuItem.itemId) {
            R.id.expandButton -> {
                historyList.applyToAll { binding, item, _ ->
                    trainingListHandler.expandItem(binding, item)
                }

            }
            R.id.collapseButton -> {
                historyList.applyToAll { binding, item, _ ->
                    trainingListHandler.collapseItem(binding, item)
                }
                historyList.notifyDataSetChanged()
                historyList.scrollToPosition(0)
            }
            R.id.totalsButton -> {
                menuItem.isChecked = !menuItem.isChecked
                trainingListHandler.showTotals = menuItem.isChecked
                historyList.notifyDataSetChanged()
            }
        }
        return false
    }

    @SuppressLint("SetTextI18n")
    private fun setHeaderInfo() {
        val trainingRowBinding = binding.trainingInclude
        val training = trainingData.training

        binding.gymName.text = training.gym.name

        val endTime = training.end
        if (endTime == null) {
            binding.time.text = String.format(
                resources.getString(R.string.compound_training_times_ongoing),
                training.start.getTimeString()
            )
        } else {
            val duration = ChronoUnit.MINUTES.between(training.start, endTime)
                .toInt()
                .minutesToTimeString()

            binding.time.text = String.format(
                resources.getString(R.string.compound_training_times),
                training.start.getTimeString(),
                endTime.getTimeString(),
                duration
            )
        }

        if (training.note.isEmpty()) {
            binding.noteRow.visibility = View.GONE
        } else {
            binding.note.text = training.note
            trainingRowBinding.notesImage.visibility = View.VISIBLE
        }

        trainingRowBinding.title.text = String.format(
            resources.getString(R.string.compound_training_date),
            training.id,
            training.start.getDateString()
        )

        trainingRowBinding.subtitle.text = trainingData.mostUsedMuscles
            .map(Muscle::text)
            .map { textRes -> resources.getString(textRes) }
            .joinToString { it }

        trainingRowBinding.indicator.setBackgroundResource(trainingData.mostUsedMuscles[0].color)

        binding.trainingDetails.apply {
            visibility = View.GONE
            trainingRowBinding.fragmentTraining.setOnClickListener {
                val anim = if (isVisible) {
                    ResizeHeightAnimation(this, 0)
                } else {
                    ResizeHeightAnimation(this)
                }
                startAnimation(anim)
            }
        }


        binding.editTraining.setOnClickListener {
            val dialog = EditTrainingDialogFragment(
                R.string.form_edit_training,
                training,
                canEditGymId,
                { result ->
                    binding.gymName.text = result.gym.name

                    val noteRowVisibility = binding.noteRow.visibility
                    if (result.note.isEmpty()) {
                        binding.noteRow.visibility = View.GONE
                        trainingRowBinding.notesImage.visibility = View.GONE
                    } else {
                        binding.noteRow.visibility = View.VISIBLE
                        trainingRowBinding.notesImage.visibility = View.VISIBLE
                        binding.note.text = result.note
                    }

                    if (noteRowVisibility != binding.noteRow.visibility) {
                        binding.trainingDetails.startAnimation(ResizeHeightAnimation(binding.trainingDetails))
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
            binding.reopenTraining.setOnClickListener {
                Data.training?.run { return@setOnClickListener }

                dbThread { db ->
                    val trainingEntity = db.trainingDao().getTraining(trainingId)
                        ?: throw LoadException("Can't find trainingId $trainingId")
                    trainingEntity.end = null
                    db.trainingDao().update(trainingEntity)
                    Data.training = Training(trainingEntity)
                }

                binding.time.text = String.format(
                    resources.getString(R.string.compound_training_times_ongoing),
                    training.start.getTimeString()
                )
                binding.reopenTraining.visibility = View.GONE
            }
        } else {
            binding.reopenTraining.visibility = View.GONE
        }

    }
}