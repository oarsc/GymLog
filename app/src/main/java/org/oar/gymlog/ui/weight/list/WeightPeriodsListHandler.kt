package org.oar.gymlog.ui.weight.list

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import org.oar.gymlog.R
import org.oar.gymlog.databinding.ListitemWeightPeriodBinding
import org.oar.gymlog.model.WeightPeriod
import org.oar.gymlog.ui.common.components.listView.CommonListView.ListElementState
import org.oar.gymlog.ui.common.components.listView.SimpleListHandler
import org.oar.gymlog.util.DateUtils.getDateString


class WeightPeriodsListHandler(
    val context: Context,
    internationalSystem: Boolean
): SimpleListHandler<WeightPeriod, ListitemWeightPeriodBinding> {
    private var onWeightPeriodClickListener: ((WeightPeriod) -> Unit)? = null
    private val unitLabel = context.getString(if (internationalSystem) R.string.text_kg else R.string.text_lb)

    override val useListState = false
    override val itemInflater: (LayoutInflater, ViewGroup?, Boolean) -> ListitemWeightPeriodBinding
        = ListitemWeightPeriodBinding::inflate

    @SuppressLint("SetTextI18n")
    override fun buildListView(
        binding: ListitemWeightPeriodBinding,
        item: WeightPeriod,
        index: Int,
        state: ListElementState?
    ) {
        binding.root.setOnClickListener {
            onWeightPeriodClickListener?.invoke(item)
        }
        binding.from.text = item.initialDate.getDateString()
        binding.to.text = item.endDate.getDateString()
        binding.subtitle.text = "+${item.expectedMuscleGain.toPlainString()}$unitLabel"
    }

    fun onWeightPeriodClicked(listener: (WeightPeriod) -> Unit) {
        onWeightPeriodClickListener = listener
    }
}