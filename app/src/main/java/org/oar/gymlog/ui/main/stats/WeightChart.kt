package org.oar.gymlog.ui.main.stats

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTH_SIDED
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import org.oar.gymlog.R
import org.oar.gymlog.service.statCalculations.DayInfo
import org.oar.gymlog.service.statCalculations.WeightCalculationResult
import org.oar.gymlog.util.DateUtils.getDateString
import org.oar.gymlog.util.extensions.CommonExts.getThemeColor
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.math.roundToInt


class WeightChart(
	private val context: Context,
	attrs: AttributeSet? = null
) : LineChart(context, attrs) {
	private var dates = emptyList<LocalDate>()
	private var weights = mapOf<LocalDate, BigDecimal>()
	private var limits = mapOf<LocalDate, DayInfo>()

	private var selectLineY: LimitLine? = null
	private var selectLineX: LimitLine? = null

	var onSelect: ((LocalDate?) -> Unit)? = null

	override fun onFinishInflate() {
		super.onFinishInflate()

		isDoubleTapToZoomEnabled = false
		isScaleYEnabled = false
		legend.isEnabled = false
		description.isEnabled = false
		xAxis.position = BOTH_SIDED

		xAxis.valueFormatter = EMPTY_FORMATTER
		axisLeft.valueFormatter = EMPTY_FORMATTER
		axisRight.valueFormatter = EMPTY_FORMATTER

		sequenceOf(xAxis, axisLeft, axisRight).forEach {
			it.gridColor = context.getColor(R.color.grayLightAlpha)
			it.axisLineColor = context.getColor(R.color.grayAlpha)
		}

		setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
			override fun onValueSelected(entry: Entry, highlight: Highlight) {
				val date = dates.getOrNull(entry.x.roundToInt()) ?: return
				select(date)
			}

			override fun onNothingSelected() {
				select(null)
			}
		})
	}

	override fun clear() {
		removeAllViews()
		super.clear()
		axisLeft.removeAllLimitLines()
		xAxis.removeAllLimitLines()
	}

	fun setInfo(weights: Map<LocalDate, BigDecimal>, context: WeightCalculationResult?) {
		val (minDate, maxDate) = context?.days?.keys
			.or { weights.keys }
			.let { it.min() to it.max() }

		this.dates = generateSequence(minDate) { if (it < maxDate) it.plusDays(1) else null }.toList()
		this.weights = weights
		this.limits = context?.days.orEmpty()
	}

	fun update() {
		clear()
		val minRangeEntries = mutableListOf<Entry>()
		val maxRangeEntries = mutableListOf<Entry>()
		val weightsEntries = dates.mapIndexedNotNull { idx, date ->
			limits[date]?.also {
				minRangeEntries.add(Entry(idx.toFloat(), it.weight.toFloat()))
				maxRangeEntries.add(Entry(idx.toFloat(), it.limitWeight.toFloat()))
			}
			weights[date]?.let { Entry(idx.toFloat(), it.toFloat()) }
		}

		val lines = buildList {
			LineDataSet(weightsEntries, "Weights").apply {
				lineWidth = 1f
				color = context.getThemeColor(android.R.attr.colorAccent)
				setDrawCircles(false)
				setDrawValues(false)
				setDrawFilled(false)
				setDrawHighlightIndicators(false)
			}.also(::add)

			LineDataSet(minRangeEntries, "MinRange").apply {
				lineWidth = 1f
				color = context.getColor(R.color.darkGray)
				setDrawCircles(false)
				setDrawValues(false)
				setDrawFilled(false)
				setDrawHighlightIndicators(false)
			}.also(::add)

			LineDataSet(maxRangeEntries, "MaxRange").apply {
				lineWidth = 1f
				color = context.getColor(R.color.darkGray)
				setDrawCircles(false)
				setDrawValues(false)
				setDrawFilled(false)
				setDrawHighlightIndicators(false)
			}.also(::add)
		}

		data = LineData(lines)

		invalidate()
	}

	fun focus(date: LocalDate = LocalDate.now()) {
		setVisibleXRangeMaximum(INITIAL_WINDOW_SIZE)
		moveViewToX(dates.indexOf(date).toFloat() - (INITIAL_WINDOW_SIZE - 5f))
		setVisibleXRangeMaximum(dates.size.toFloat() - 20f)
	}

	fun select(date: LocalDate? = LocalDate.now()) {
		if (date == null) {
			selectLineX?.also(xAxis::removeLimitLine)
			selectLineX = null
			selectLineY?.also(axisLeft::removeLimitLine)
			selectLineY = null
			onSelect?.invoke(null)
			return
		}

		val x = dates.indexOf(date)
		val weight = weights[date]?.toFloat()

		selectLineY?.also(axisLeft::removeLimitLine)
		selectLineX?.also(xAxis::removeLimitLine)

		if (weight != null) {
			selectLineY = LimitLine(weight, "%.2fkg".format(weight)).apply {
				lineWidth = 1f
				lineColor = Color.GRAY
				textSize = 10f
				labelPosition = LimitLine.LimitLabelPosition.RIGHT_BOTTOM
				textColor = context.getThemeColor(android.R.attr.colorPrimary)
			}
			axisLeft.addLimitLine(selectLineY)
		}

		selectLineX = LimitLine(x.toFloat(), date.getDateString()).apply {
			lineWidth = 1f
			lineColor = Color.GRAY
			textSize = 10f
			textColor = context.getThemeColor(android.R.attr.colorPrimary)
		}
		xAxis.addLimitLine(selectLineX)
		invalidate()

		onSelect?.invoke(date)
	}

	private fun <A> A?.or(elseGenerate: () -> A): A = this ?: elseGenerate()

	private companion object {
		private const val INITIAL_WINDOW_SIZE = 50f
		private val EMPTY_FORMATTER = object : ValueFormatter() {
			override fun getFormattedValue(value: Float) = ""
		}
	}
}
