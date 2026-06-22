package org.oar.gymlog.ui.main.stats

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import org.oar.gymlog.R
import org.oar.gymlog.databinding.ListitemSeparatorBinding
import org.oar.gymlog.databinding.ListitemWeightBinding
import org.oar.gymlog.ui.common.components.listView.CommonListView
import org.oar.gymlog.ui.common.components.listView.MultipleListHandler
import org.oar.gymlog.ui.main.stats.rows.IWeightRow
import org.oar.gymlog.ui.main.stats.rows.WeightRow
import org.oar.gymlog.ui.main.stats.rows.WeightSeparatorRow
import org.oar.gymlog.util.Constants.ONE_HUNDRED
import org.oar.gymlog.util.DateUtils.getDateString
import org.oar.gymlog.util.extensions.CommonExts.getThemeColor
import java.time.LocalDate
import kotlin.math.roundToInt

class WeightListHandler(
	private val context: Context,
	private val unitLabel: String
) : MultipleListHandler<IWeightRow> {
	private val defaultDayColor = context.getThemeColor(android.R.attr.colorSecondary)

	override val useListState = false
	override val itemInflaters = listOf<(LayoutInflater, ViewGroup?, Boolean) -> ViewBinding>(
		ListitemWeightBinding::inflate,
		ListitemSeparatorBinding::inflate
	)

	private var onClickListener: ((WeightRow, Int) -> Unit)? = null

	override fun findItemInflaterIndex(item: IWeightRow) = when(item) {
		is WeightRow -> 0
		is WeightSeparatorRow -> 1
		else -> error("Wrong item type \"${item::class.simpleName}\"")
	}

	override fun buildListView(
		binding: ViewBinding,
		item: IWeightRow,
		index: Int,
		state: CommonListView.ListElementState?
	) {
		when(item) {
			is WeightRow -> {
				binding as ListitemWeightBinding

				binding.apply {
					root.setOnClickListener { onClickListener?.invoke(item, index) }
					if (item.day == LocalDate.now()) {
//						day.setTextColor(context.getThemeColor(android.R.attr.colorAccent))
						day.setTypeface(null, Typeface.BOLD)
						today.visibility = View.VISIBLE
					} else {
//						day.setTextColor(defaultDayColor)
						day.setTypeface(null, Typeface.NORMAL)
						today.visibility = View.INVISIBLE
					}
					day.alpha = if (item.day.isWeekend) 0.5f else 1f

					val tint = when (item.isBulkDay) {
						true -> context.getColor(R.color.bulkPeriod)
						false -> context.getColor(R.color.leanPeriod)
						null -> context.getThemeColor(android.R.attr.colorPrimary)
					}.let { ColorStateList.valueOf(it) }

					indicator1.backgroundTintList = tint
					indicator2.backgroundTintList = tint

					if (item.manualWeight != null && item.weight != null && item.limitWeight != null) {
						val percent = ((item.manualWeight - item.weight) / (item.limitWeight - item.weight) * ONE_HUNDRED).toFloat()

						when {
							percent < 0 -> R.color.blue
							percent > 100 -> R.color.red
							else -> R.color.gray
						}.also { position.setTextColor(context.getColor(it)) }

						position.text = percent.roundToInt().toString()
					} else {
						position.text = null
					}

					range.visibility = if (item.weight == null) View.INVISIBLE else View.VISIBLE

					day.text = item.day.getDateString()
					weight.text = item.manualWeight?.toPlainString()?.let { "$it$unitLabel"}
					min.text = item.weight?.toPlainString()?.let { "$it$unitLabel"}
					max.text = item.limitWeight?.toPlainString()?.let { "$it$unitLabel"}
				}
			}
		}
	}

	fun setOnClickListener(listener: (WeightRow, Int) -> Unit) {
		onClickListener = listener
	}

	private val LocalDate.isWeekend get() = this.dayOfWeek.value > 5
}
