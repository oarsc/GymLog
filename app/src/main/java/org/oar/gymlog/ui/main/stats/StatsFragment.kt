package org.oar.gymlog.ui.main.stats

import android.content.Intent
import android.icu.text.DecimalFormatSymbols
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import org.oar.gymlog.R
import org.oar.gymlog.databinding.FragmentStatsBinding
import org.oar.gymlog.model.Weight
import org.oar.gymlog.room.entities.WeightEntity
import org.oar.gymlog.service.PeriodCalculationService
import org.oar.gymlog.ui.common.ResultLauncherFragment
import org.oar.gymlog.ui.common.components.listView.MultipleListView.Companion.cast
import org.oar.gymlog.ui.common.dialogs.EditTextDialogFragment
import org.oar.gymlog.ui.main.preferences.PreferencesDefinition
import org.oar.gymlog.ui.main.stats.rows.IWeightRow
import org.oar.gymlog.ui.main.stats.rows.WeightRow
import org.oar.gymlog.ui.main.stats.rows.WeightSeparatorRow
import org.oar.gymlog.ui.weight.create.CreateWeightPeriodActivity
import org.oar.gymlog.ui.weight.details.WeightDetailsActivity
import org.oar.gymlog.ui.weight.list.WeightPeriodsActivity
import org.oar.gymlog.util.Constants.IntentReference
import org.oar.gymlog.util.Data
import org.oar.gymlog.util.DateUtils.getDateString
import org.oar.gymlog.util.FormatUtils.bigDecimal
import org.oar.gymlog.util.FormatUtils.safeBigDecimal
import org.oar.gymlog.util.extensions.CommonExts.multiplyByHundred
import org.oar.gymlog.util.extensions.ComponentsExts.runOnUiThread
import org.oar.gymlog.util.extensions.DataExts.updateTodayWeightPeriod
import org.oar.gymlog.util.extensions.DatabaseExts.dbThread
import org.oar.gymlog.util.extensions.PreferencesExts.loadBoolean
import java.math.BigDecimal.ZERO
import java.math.RoundingMode.HALF_UP
import java.time.LocalDate

class StatsFragment : ResultLauncherFragment() {
	private var internationalSystem = false
	private lateinit var binding: FragmentStatsBinding

	private var today = LocalDate.now()
	private var unitLabel = ""
	private var todayWeightValue = "0.00".toBigDecimal()
	private var weightCalculationResult = Data.weightPeriod?.let(::PeriodCalculationService)?.execute()
	private val rows = mutableListOf<IWeightRow>()

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View = FragmentStatsBinding.inflate(inflater, container, false)
		.apply {
			binding = this
			internationalSystem = requireContext().loadBoolean(PreferencesDefinition.UNIT_INTERNATIONAL_SYSTEM)
			unitLabel = requireContext().getString(if (internationalSystem) R.string.text_kg else R.string.text_lb)

			toolbar.setNavigationOnClickListener {
				requireActivity().onBackPressedDispatcher.onBackPressed()
			}

			toolbar.setOnMenuItemClickListener { item: MenuItem ->
				when (item.itemId) {
					R.id.listButton -> {
						val intent = Intent(context, WeightPeriodsActivity::class.java)
						startActivityForResult(intent, IntentReference.LIST_WEIGHT_PERIOD_DETAILS)
						return@setOnMenuItemClickListener true
					}
					R.id.detailsButton -> {
						val intent = Intent(requireContext(), WeightDetailsActivity::class.java)
						intent.putExtra("weightId", weightCalculationResult!!.weightPeriod.id)
						startActivityForResult(intent, IntentReference.WEIGHT_PERIOD_DETAILS)
						return@setOnMenuItemClickListener true
					}
					R.id.createButton -> {
						val intent = Intent(context, CreateWeightPeriodActivity::class.java)
						startActivityForResult(intent, IntentReference.CREATE_WEIGHT_PERIOD_DETAILS)
						return@setOnMenuItemClickListener true
					}
				}
				false
			}

			saveButton.setOnClickListener {
				persistCurrentWeight()
			}

			unit.text = unitLabel

			val decimalSeparator = DecimalFormatSymbols.getInstance().decimalSeparator
			input.filters = arrayOf(
				InputFilter { source, start, end, _, _, _ ->
					source
						?.subSequence(start, end)
						?.toString()
						?.replace(',', decimalSeparator)
						?.replace('.', decimalSeparator)
				}
			)

			loadData()
		}
		.root

	private fun loadData() {
		binding.apply {
			binding.toolbar.menu.findItem(R.id.detailsButton).isVisible = weightCalculationResult != null

			if (weightCalculationResult == null) {
				headerBlock.visibility = View.GONE
			} else {
				headerBlock.visibility = View.VISIBLE
				val weightPeriod = weightCalculationResult!!.weightPeriod
				header.root.setOnClickListener {
					val intent = Intent(requireContext(), WeightDetailsActivity::class.java)
					intent.putExtra("weightId", weightPeriod.id)
					startActivityForResult(intent, IntentReference.WEIGHT_PERIOD_DETAILS)
				}
				header.from.text = weightPeriod.initialDate.getDateString()
				header.to.text = weightPeriod.endDate.getDateString()
				header.subtitle.text = "+${weightPeriod.expectedMuscleGain.toPlainString()}$unitLabel"
			}

			fillRows()
			weightList.cast<IWeightRow>().apply {
				val handler = WeightListHandler(requireContext(), unitLabel)
				init(rows, handler)
				listFocusToday()
				handler.setOnClickListener { row, idx ->
					if (row.day <= today) {
						EditTextDialogFragment(
							title = R.string.text_weight,
							initialValue = row.manualWeight?.toPlainString() ?: "",
							inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL,
							confirm = { result -> persistWeight(row, idx, result) }
						).show(requireActivity().supportFragmentManager, null)
					}
				}
			}

			Data.weights[today]
				?.getValue(internationalSystem)
				?.also { todayWeightValue = it }

			input.setText(todayWeightValue.toPlainString())
			input.doAfterTextChanged { text ->
				if (input.bigDecimal.compareTo(todayWeightValue) == 0) {
					saveButton.alpha = 0.5f
					saveButton.isClickable = false
				} else {
					saveButton.alpha = 1f
					saveButton.isClickable = true
				}
			}
		}
	}

	private fun fillRows() {
		val (minDate, maxDate) = weightCalculationResult
			?.let { it.days.keys.min() to it.days.keys.max() }
			?: run { today.minusYears(1) to today.plusDays(7) }

		var currentDate = minDate
		var lastMonth = currentDate.month
		while (currentDate <= maxDate) {
			val dayWeight = Data.weights[currentDate]
			val dayResult = weightCalculationResult?.days?.get(currentDate)
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

	private fun listFocusToday() {
		val index = rows.indexOf(today)

		if (index >= 0) {
			binding.weightList.scrollToPosition(index, 300)
		} else if (rows.isNotEmpty()) {
			binding.weightList.scrollToPosition(rows.size - 1, 300)
		}
	}

	private fun persistCurrentWeight() {
		val exists = Data.weights.containsKey(today)

		todayWeightValue = binding.input.bigDecimal.setScale(2, HALF_UP)
		val toRemove = todayWeightValue <= ZERO
		val todayWeight = Weight(todayWeightValue, internationalSystem)

		binding.saveButton.alpha = 0.4f
		binding.saveButton.isClickable = false

		dbThread { db ->
			val entity = WeightEntity().apply {
				date = today
				weight = todayWeightValue.multiplyByHundred()
				kilos = internationalSystem
			}

			if (exists) {
				if (toRemove) {
					db.weightDao().delete(entity)
					Data.weights.remove(today)
				} else {
					db.weightDao().update(entity)
					Data.weights[today] = todayWeight
				}
			} else {
				db.weightDao().insert(entity)
				Data.weights[today] = todayWeight
			}
		}

		val index = rows.indexOf(today)
		if (index >= 0) {
			binding.weightList.update(rows.getWeightRow(index).copy(manualWeight = if (toRemove) null else todayWeightValue), index)
		} else if (!toRemove) {
			if (rows.getWeightRow(rows.size - 1).day.month != today.month) {
				binding.weightList.add(WeightSeparatorRow())
			}
			binding.weightList.add(
				WeightRow(
					day = today,
					manualWeight = todayWeightValue,
				)
			)
		}
	}

	private fun List<IWeightRow>.getWeightRow(index: Int) = this[index] as WeightRow
	private fun List<IWeightRow>.indexOf(date: LocalDate): Int = this.indexOfFirst {
        if (it is WeightRow) it.day == date
        else false
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
	}

	override fun onActivityResult(intentReference: IntentReference, data: Intent) {
		when(intentReference) {
			IntentReference.LIST_WEIGHT_PERIOD_DETAILS,
			IntentReference.CREATE_WEIGHT_PERIOD_DETAILS,
			IntentReference.WEIGHT_PERIOD_DETAILS -> {
				if (data.getBooleanExtra("refresh", false)) {
					rows.clear()
					dbThread { db ->
						db.updateTodayWeightPeriod()
						weightCalculationResult = Data.weightPeriod?.let(::PeriodCalculationService)?.execute()
						runOnUiThread { loadData() }
					}
				}
			}
			else -> { }
		}
	}

	override fun onPause() {
		super.onPause()
		binding.input.setText(todayWeightValue.toPlainString())
		binding.input.clearFocus()
	}

	override fun onResume() {
		super.onResume()
		val today = LocalDate.now()
		if (today == this.today) {
			listFocusToday()
		} else {
			this.today = today
			this.todayWeightValue = "0.00".toBigDecimal()
			loadData()
		}
	}
}
