package org.oar.gymlog.ui.weight.details

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import org.oar.gymlog.databinding.ListitemWeightPeriodModificationAddBinding
import org.oar.gymlog.databinding.ListitemWeightPeriodModificationBinding
import org.oar.gymlog.model.WeightPeriodModification
import org.oar.gymlog.ui.common.components.listView.CommonListView.ListElementState
import org.oar.gymlog.ui.common.components.listView.MultipleListHandler
import org.oar.gymlog.ui.weight.details.rows.IWeightPeriodModificationRow
import org.oar.gymlog.ui.weight.details.rows.WeightPeriodModificationCreateRow
import org.oar.gymlog.ui.weight.details.rows.WeightPeriodModificationRow
import org.oar.gymlog.util.DateUtils.getDateString

class WeightModificationsListHandler(
    val context: Context
): MultipleListHandler<IWeightPeriodModificationRow> {
    override val useListState = false
    override val itemInflaters = listOf<(LayoutInflater, ViewGroup?, Boolean) -> ViewBinding>(
        ListitemWeightPeriodModificationBinding::inflate,
        ListitemWeightPeriodModificationAddBinding::inflate,
    )

    override fun findItemInflaterIndex(item: IWeightPeriodModificationRow) = when(item) {
        is WeightPeriodModificationRow -> 0
        is WeightPeriodModificationCreateRow -> 1
        else -> error("Wrong item type \"${item::class.simpleName}\"")
    }

    private var onClickListener: ((WeightPeriodModification?) -> Unit)? = null

    fun onSetClicked(listener: (WeightPeriodModification?) -> Unit) {
        onClickListener = listener
    }

    @SuppressLint("SetTextI18n")
    override fun buildListView(
        binding: ViewBinding,
        item: IWeightPeriodModificationRow,
        index: Int,
        state: ListElementState?
    ) {
        when(item) {
            is WeightPeriodModificationRow -> {
                binding as ListitemWeightPeriodModificationBinding
                val weightPeriodModification = item.weightPeriodModification

                binding.from.text = weightPeriodModification.initialDate.getDateString()
                binding.to.text = weightPeriodModification.endDate.getDateString()
                binding.subtitle.text = "${weightPeriodModification.gramsPerWeek.withSymbol()}g"

                onClickListener?.also { listener ->
                    binding.root.setOnClickListener { listener(weightPeriodModification) }
                }
            }
            is WeightPeriodModificationCreateRow -> {
                onClickListener?.also { listener ->
                    binding.root.setOnClickListener { listener(null) }
                }
            }
            else -> {}
        }
    }

    private fun Int.withSymbol(): String = if (this > 0) "+$this" else toString()
}