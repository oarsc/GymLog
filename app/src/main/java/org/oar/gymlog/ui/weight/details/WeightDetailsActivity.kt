package org.oar.gymlog.ui.weight.details

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import org.oar.gymlog.R
import org.oar.gymlog.databinding.ActivityWeightPeriodDetailsBinding
import org.oar.gymlog.model.WeightPeriod
import org.oar.gymlog.model.WeightPeriodModification
import org.oar.gymlog.room.AppDatabase
import org.oar.gymlog.service.PeriodCalculationService
import org.oar.gymlog.service.statCalculations.WeightCalculationResult
import org.oar.gymlog.ui.common.DatabaseAppCompatActivity
import org.oar.gymlog.ui.common.components.listView.MultipleListView
import org.oar.gymlog.ui.common.components.listView.MultipleListView.Companion.cast
import org.oar.gymlog.ui.common.dialogs.MenuDialogFragment
import org.oar.gymlog.ui.weight.create.CreateWeightPeriodActivity
import org.oar.gymlog.ui.weight.createModification.CreateWeightPeriodModificationActivity
import org.oar.gymlog.ui.weight.details.rows.IWeightPeriodModificationRow
import org.oar.gymlog.ui.weight.details.rows.WeightPeriodModificationCreateRow
import org.oar.gymlog.ui.weight.details.rows.WeightPeriodModificationRow
import org.oar.gymlog.util.Constants.IntentReference
import org.oar.gymlog.util.DateUtils.getDateString
import org.oar.gymlog.util.extensions.ComponentsExts.mustRefreshParent
import org.oar.gymlog.util.extensions.DatabaseExts.dbThread
import java.math.BigDecimal
import java.math.RoundingMode

class WeightDetailsActivity : DatabaseAppCompatActivity<ActivityWeightPeriodDetailsBinding>(ActivityWeightPeriodDetailsBinding::inflate) {
    private lateinit var weightPeriod: WeightPeriod
    private lateinit var weightCalculationResult: WeightCalculationResult

    private lateinit var modificationsList: MultipleListView<IWeightPeriodModificationRow>

    override fun onLoad(savedInstanceState: Bundle?, db: AppDatabase): Int {
        val entities = intent.extras!!.getInt("weightId").let(db.weightDao()::getPeriodWithModifications)
        weightPeriod = WeightPeriod(entities.weightPeriod!!).apply {
            entities.modifications.forEach {
                modifications.add(WeightPeriodModification(it, this))
            }
        }
        weightCalculationResult = PeriodCalculationService(weightPeriod).execute()
        return CONTINUE
    }

    override fun onDelayedCreate(savedInstanceState: Bundle?) = loadInfo()

    @SuppressLint("SetTextI18n")
    private fun loadInfo() {
        binding.apply {
            toolbar.apply {
                setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
                setOnMenuItemClickListener(::onOptionsItemSelected)
            }

            startingDate.text = weightPeriod.initialDate.getDateString()
            endingDate.text = weightPeriod.endDate.getDateString()

            startingWeight.text = weightPeriod.initialWeight.toWeightString()
            expectedWeight.text = (weightPeriod.initialWeight + weightPeriod.expectedMuscleGain).toWeightString()

            gainWeek.text = weightPeriod.gainGramsPerWeek.toGramsString()
            lossWeek.text = weightPeriod.lossGramsPerWeek.toGramsString()

            tolerance.text = (weightPeriod.toleranceGrams / 1000f).toWeightString()

            updateCalculation()
        }
        val handler = WeightModificationsListHandler(this)
        modificationsList = binding.modificationsList.cast()
        modificationsList.init(buildRows(), handler)

        handler.onSetClicked { weightPeriodModification ->
            if (weightPeriodModification == null) {
                val intent = Intent(this, CreateWeightPeriodModificationActivity::class.java)
                intent.putExtra("weightPeriodId", weightPeriod.id)
                startActivityForResult(intent, IntentReference.CREATE_WEIGHT_PERIOD_MODIFICATION_DETAILS)
            } else {
                MenuDialogFragment(R.menu.weight_period_details_modification_menu) {
                    when(it) {
                        R.id.removeButton -> {
                            dbThread { db ->
                                db.weightDao().deleteModification(weightPeriodModification.toEntity())
                                val idx = weightPeriod.modifications.indexOf(weightPeriodModification)
                                if (idx >= 0) {
                                    weightPeriod.modifications.removeAt(idx)
                                    weightCalculationResult = PeriodCalculationService(weightPeriod).execute()
                                    runOnUiThread {
                                        modificationsList.removeAt(idx)
                                        updateCalculation()
                                    }
                                }
                            }
                            mustRefreshParent()
                        }
                        R.id.editButton -> {
                            val intent = Intent(this, CreateWeightPeriodModificationActivity::class.java)
                            intent.putExtra("weightPeriodId", weightPeriodModification.weightPeriod.id)
                            intent.putExtra("weightPeriodModificationId", weightPeriodModification.id)
                            startActivityForResult(intent, IntentReference.EDIT_WEIGHT_PERIOD_MODIFICATION_DETAILS)
                        }
                    }
                }.show(supportFragmentManager, null)
            }
        }
    }

    private fun updateCalculation() {
        binding.apply {
            totalDays.text = weightCalculationResult.days.size.toString()
            bulkDays.text = weightCalculationResult.bulkDays.toString()
            leanDate.text = weightCalculationResult.switchDate.getDateString()
            maxWeight.text = weightCalculationResult.days.maxOf { it.value.limitWeight }.toWeightString()
        }
    }

    private fun buildRows(): List<IWeightPeriodModificationRow> =
        weightPeriod.modifications.map(::WeightPeriodModificationRow) + WeightPeriodModificationCreateRow()

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.editButton -> {
            val intent = Intent(this, CreateWeightPeriodActivity::class.java)
            intent.putExtra("weightPeriodId", weightPeriod.id)
            startActivityForResult(intent, IntentReference.EDIT_WEIGHT_PERIOD_DETAILS)
            true
        }
        else -> false
    }

    private fun BigDecimal.toWeightString() = setScale(2, RoundingMode.HALF_UP).toPlainString() + "kg"
    private fun Float.toWeightString() = toBigDecimal().toWeightString()
    private fun Int.toGramsString() = "${this}g"

    override fun onActivityResult(intentReference: IntentReference, data: Intent) {
        when(intentReference) {
            IntentReference.EDIT_WEIGHT_PERIOD_MODIFICATION_DETAILS,
            IntentReference.CREATE_WEIGHT_PERIOD_MODIFICATION_DETAILS,
            IntentReference.EDIT_WEIGHT_PERIOD_DETAILS -> {
                if (data.getBooleanExtra("refresh", false)) {
                    dbThread { db ->
                        val entities = intent.extras!!.getInt("weightId").let(db.weightDao()::getPeriodWithModifications)
                        weightPeriod = WeightPeriod(entities.weightPeriod!!).apply {
                            entities.modifications.forEach {
                                modifications.add(WeightPeriodModification(it, this))
                            }
                        }
                        weightCalculationResult = PeriodCalculationService(weightPeriod).execute()
                        runOnUiThread {
                            loadInfo()

                            if (intentReference == IntentReference.EDIT_WEIGHT_PERIOD_MODIFICATION_DETAILS ||
                                intentReference == IntentReference.CREATE_WEIGHT_PERIOD_MODIFICATION_DETAILS) {
                                val initSize = modificationsList.size
                                modificationsList.setListData(buildRows())
                                modificationsList.dynamicallyItemsChangedBySize(initSize)
                            }
                        }
                    }
                    mustRefreshParent()
                }
            }
            else -> {}
        }
    }
}