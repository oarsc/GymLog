package org.oar.gymlog.ui.weight.list

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import org.oar.gymlog.R
import org.oar.gymlog.databinding.ActivityWeightPeriodsBinding
import org.oar.gymlog.databinding.ListitemWeightPeriodBinding
import org.oar.gymlog.model.WeightPeriod
import org.oar.gymlog.room.AppDatabase
import org.oar.gymlog.ui.common.DatabaseAppCompatActivity
import org.oar.gymlog.ui.common.components.listView.SimpleListView
import org.oar.gymlog.ui.common.components.listView.SimpleListView.Companion.cast
import org.oar.gymlog.ui.main.preferences.PreferencesDefinition
import org.oar.gymlog.ui.weight.create.CreateWeightPeriodActivity
import org.oar.gymlog.ui.weight.stats.PeriodStatsActivity
import org.oar.gymlog.util.Constants.IntentReference
import org.oar.gymlog.util.extensions.ComponentsExts.mustRefreshParent
import org.oar.gymlog.util.extensions.DatabaseExts.dbThread
import org.oar.gymlog.util.extensions.PreferencesExts.loadBoolean

class WeightPeriodsActivity : DatabaseAppCompatActivity<ActivityWeightPeriodsBinding>(ActivityWeightPeriodsBinding::inflate) {
    private lateinit var weightPeriodsWithoutModifications: List<WeightPeriod>
    private var internationalSystem: Boolean = false

    private lateinit var exercisesListView: SimpleListView<WeightPeriod, ListitemWeightPeriodBinding>

    override fun onLoad(savedInstanceState: Bundle?, db: AppDatabase): Int {
        internationalSystem = loadBoolean(PreferencesDefinition.UNIT_INTERNATIONAL_SYSTEM)
        weightPeriodsWithoutModifications = db.weightDao().getAllPeriods().map(::WeightPeriod)
        return CONTINUE
    }

    override fun onDelayedCreate(savedInstanceState: Bundle?) {
        binding.toolbar.apply {
            setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
            setOnMenuItemClickListener(::onOptionsItemSelected)
        }

        exercisesListView = binding.exercisesList.cast()
        WeightPeriodsListHandler(this, internationalSystem).apply {
            exercisesListView.init(weightPeriodsWithoutModifications, this)

            onWeightPeriodClicked {
                val intent = Intent(this@WeightPeriodsActivity, PeriodStatsActivity::class.java)
                intent.putExtra("weightPeriodId", it.id)
                startActivityForResult(intent, IntentReference.WEIGHT_PERIOD_STATS_DETAILS)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.createButton -> {
                val intent = Intent(this, CreateWeightPeriodActivity::class.java)
                startActivityForResult(intent, IntentReference.CREATE_WEIGHT_PERIOD_DETAILS)
                return true
            }
        }
        return false
    }

    override fun onActivityResult(intentReference: IntentReference, data: Intent) {
        when (intentReference) {
            IntentReference.WEIGHT_PERIOD_STATS_DETAILS,
            IntentReference.CREATE_WEIGHT_PERIOD_DETAILS -> {
                if (data.getBooleanExtra("refresh", false)) {
                    dbThread { db ->
                        weightPeriodsWithoutModifications = db.weightDao().getAllPeriods().map(::WeightPeriod)

                        runOnUiThread {
                            val initSize = exercisesListView.size
                            exercisesListView.setListData(weightPeriodsWithoutModifications)
                            exercisesListView.dynamicallyItemsChangedBySize(initSize)
                        }
                    }
                    mustRefreshParent()
                }
            }
            else -> {}
        }
    }
}