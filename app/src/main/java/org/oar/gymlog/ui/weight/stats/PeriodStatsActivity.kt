package org.oar.gymlog.ui.weight.stats

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.MenuItem
import android.view.View
import org.oar.gymlog.R
import org.oar.gymlog.databinding.ActivityPeriodStatsBinding
import org.oar.gymlog.model.Weight
import org.oar.gymlog.model.WeightPeriod
import org.oar.gymlog.model.WeightPeriodModification
import org.oar.gymlog.room.AppDatabase
import org.oar.gymlog.room.entities.WeightEntity
import org.oar.gymlog.service.PeriodCalculationService
import org.oar.gymlog.service.statCalculations.WeightCalculationResult
import org.oar.gymlog.ui.common.DatabaseAppCompatActivity
import org.oar.gymlog.ui.common.components.listView.MultipleListView.Companion.cast
import org.oar.gymlog.ui.common.dialogs.EditTextDialogFragment
import org.oar.gymlog.ui.common.dialogs.TextDialogFragment
import org.oar.gymlog.ui.main.preferences.PreferencesDefinition
import org.oar.gymlog.ui.main.stats.WeightListHandler
import org.oar.gymlog.ui.main.stats.rows.IWeightRow
import org.oar.gymlog.ui.main.stats.rows.WeightRow
import org.oar.gymlog.ui.main.stats.rows.WeightSeparatorRow
import org.oar.gymlog.ui.weight.details.WeightDetailsActivity
import org.oar.gymlog.util.Constants.IntentReference
import org.oar.gymlog.util.Data
import org.oar.gymlog.util.DateUtils.getDateString
import org.oar.gymlog.util.FormatUtils.safeBigDecimal
import org.oar.gymlog.util.extensions.CommonExts.multiplyByHundred
import org.oar.gymlog.util.extensions.ComponentsExts.mustRefreshParent
import org.oar.gymlog.util.extensions.DatabaseExts.dbThread
import org.oar.gymlog.util.extensions.PreferencesExts.loadBoolean
import java.math.BigDecimal.ZERO
import java.math.RoundingMode.HALF_UP
import java.time.LocalDate

class PeriodStatsActivity : DatabaseAppCompatActivity<ActivityPeriodStatsBinding>(ActivityPeriodStatsBinding::inflate) {
    private lateinit var weightPeriod: WeightPeriod
    private lateinit var weightCalculationResult: WeightCalculationResult

    private var internationalSystem = false
    private var unitLabel = ""
    private val rows = mutableListOf<IWeightRow>()

    override fun onLoad(savedInstanceState: Bundle?, db: AppDatabase): Int {
        internationalSystem = loadBoolean(PreferencesDefinition.UNIT_INTERNATIONAL_SYSTEM)
        db.recalculate(intent.extras!!.getInt("weightPeriodId"))
        return CONTINUE
    }

    private fun AppDatabase.recalculate(weightPeriodId: Int) {
        val entities = weightDao().getPeriodWithModifications(weightPeriodId)
        weightPeriod = WeightPeriod(entities.weightPeriod!!).apply {
            entities.modifications.forEach {
                modifications.add(WeightPeriodModification(it, this))
            }
        }
        weightCalculationResult = PeriodCalculationService(weightPeriod).execute()
    }

    override fun onDelayedCreate(savedInstanceState: Bundle?) {
        binding.toolbar.apply {
            setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
            setOnMenuItemClickListener(::onOptionsItemSelected)
        }
        loadData()
    }

    private fun loadData() {
        binding.apply {
            headerBlock.visibility = View.VISIBLE
            val weightPeriod = weightCalculationResult.weightPeriod
            header.root.setOnClickListener {
                val intent = Intent(this@PeriodStatsActivity, WeightDetailsActivity::class.java)
                intent.putExtra("weightId", weightPeriod.id)
                startActivityForResult(intent, IntentReference.WEIGHT_PERIOD_DETAILS)
            }
            header.from.text = weightPeriod.initialDate.getDateString()
            header.to.text = weightPeriod.endDate.getDateString()
            header.subtitle.text = "+${weightPeriod.expectedMuscleGain.toPlainString()}$unitLabel"

            fillRows()
            weightList.cast<IWeightRow>().apply {
                val handler = WeightListHandler(this@PeriodStatsActivity, unitLabel)
                init(rows, handler)
                handler.setOnClickListener { row, idx ->
                    if (row.day <= LocalDate.now()) {
                        EditTextDialogFragment(
                            title = R.string.text_weight,
                            initialValue = row.manualWeight?.toPlainString() ?: "",
                            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL,
                            confirm = { result -> persistWeight(row, idx, result) }
                        ).show(supportFragmentManager, null)
                    }
                }
            }
        }
    }

    private fun fillRows() {
        val minDate = weightCalculationResult.days.keys.min()
        val maxDate = weightCalculationResult.days.keys.max()

        var currentDate = minDate
        var lastMonth = currentDate.month
        while (currentDate <= maxDate) {
            val dayWeight = Data.weights[currentDate]
            val dayResult = weightCalculationResult.days[currentDate]
            rows.add(
                WeightRow(
                    day = currentDate,
                    weight = dayResult?.weight,
                    limitWeight = dayResult?.limitWeight,
                    manualWeight = dayWeight?.getValue(internationalSystem),
                    isBulkDay = dayResult?.isBulkDay
                )
            )
            currentDate = currentDate.plusDays(1)
            if (currentDate.month != lastMonth) {
                lastMonth = currentDate.month
                rows.add(WeightSeparatorRow())
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.detailsButton -> {
            val intent = Intent(this, WeightDetailsActivity::class.java)
            intent.putExtra("weightId", weightCalculationResult.weightPeriod.id)
            startActivityForResult(intent, IntentReference.WEIGHT_PERIOD_DETAILS)
            true
        }
        R.id.removeButton -> {
            TextDialogFragment(
                R.string.dialog_confirm_remove_weight_period_title,
                R.string.dialog_confirm_remove_weight_period_text
            ) { confirmed ->
                if (confirmed) {
                    dbThread { db ->
                        db.weightDao().deletePeriod(weightPeriod.toEntity())
                        runOnUiThread {
                            mustRefreshParent()
                            finish()
                        }
                    }
                }
            }.show(supportFragmentManager, null)
            true
        }
        else -> false
    }

    private fun persistWeight(row: WeightRow, index: Int, input: String) {
        val date = row.day
        val exists = Data.weights.containsKey(date)

        val weightValue = input.safeBigDecimal().setScale(2, HALF_UP)
        val toRemove = weightValue <= ZERO
        val weight = Weight(weightValue, internationalSystem)

        dbThread { db ->
            val entity = WeightEntity().apply {
                this.date = date
                this.weight = weightValue.multiplyByHundred()
                this.kilos = internationalSystem
            }

            if (exists) {
                if (toRemove) {
                    db.weightDao().delete(entity)
                    Data.weights.remove(date)
                } else {
                    db.weightDao().update(entity)
                    Data.weights[date] = weight
                }
            } else {
                db.weightDao().insert(entity)
                Data.weights[date] = weight
            }
        }
        binding.weightList.update(rows.getWeightRow(index).copy(manualWeight = if (toRemove) null else weightValue), index)
        mustRefreshParent()
    }

    private fun List<IWeightRow>.getWeightRow(index: Int) = this[index] as WeightRow

    override fun onActivityResult(intentReference: IntentReference, data: Intent) {
        when(intentReference) {
            IntentReference.WEIGHT_PERIOD_DETAILS -> {
                if (data.getBooleanExtra("refresh", false)) {
                    rows.clear()
                    dbThread { db ->
                        db.recalculate(weightCalculationResult.weightPeriod.id)
                        runOnUiThread {
                            loadData()
                        }
                    }
                    mustRefreshParent()
                }
            }
            else -> {}
        }
    }
}