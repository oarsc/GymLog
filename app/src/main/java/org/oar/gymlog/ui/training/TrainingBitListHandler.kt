package org.oar.gymlog.ui.training

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import org.oar.gymlog.R
import org.oar.gymlog.databinding.ListitemHistoryBitBinding
import org.oar.gymlog.databinding.ListitemHistoryHeadersBinding
import org.oar.gymlog.model.Bit
import org.oar.gymlog.model.ExerciseType
import org.oar.gymlog.model.WeightSpecification
import org.oar.gymlog.ui.common.components.listView.CommonListView
import org.oar.gymlog.ui.common.components.listView.MultipleListHandler
import org.oar.gymlog.ui.training.rows.ITrainingBitRow
import org.oar.gymlog.ui.training.rows.TrainingBitRow
import org.oar.gymlog.util.DateUtils.getTimeString
import org.oar.gymlog.util.FormatUtils.bigDecimal
import org.oar.gymlog.util.WeightUtils.calculate
import java.util.function.BiConsumer

class TrainingBitListHandler(
    private val internationalSystem: Boolean,
    private val showTotals: Boolean,
) : MultipleListHandler<ITrainingBitRow> {
    override val useListState = false
    override val itemInflaters = listOf<(LayoutInflater, ViewGroup?, Boolean) -> ViewBinding>(
        ListitemHistoryHeadersBinding::inflate,
        ListitemHistoryBitBinding::inflate,
    )

    override fun findItemInflaterIndex(item: ITrainingBitRow) = when (item.type) {
        ITrainingBitRow.Type.HEADER,
        ITrainingBitRow.Type.HEADER_SUPERSET -> 0
        ITrainingBitRow.Type.BIT,
        ITrainingBitRow.Type.BIT_SUPERSET -> 1
    }

    private var onClickListener: BiConsumer<Bit, Int>? = null

    fun setOnClickListener(onClickListener: BiConsumer<Bit, Int>) {
        this.onClickListener = onClickListener
    }

    override fun buildListView(
        binding: ViewBinding,
        item: ITrainingBitRow,
        index: Int,
        state: CommonListView.ListElementState?
    ) {
        when (item.type) {
            ITrainingBitRow.Type.HEADER,
            ITrainingBitRow.Type.HEADER_SUPERSET -> {
                binding as ListitemHistoryHeadersBinding
                binding.hashColumn.visibility = View.GONE
            }

            ITrainingBitRow.Type.BIT,
            ITrainingBitRow.Type.BIT_SUPERSET -> {
                binding as ListitemHistoryBitBinding
                item as TrainingBitRow

                val bit = item.bit

                val weightSpec =
                    if (showTotals && bit.variation.type != ExerciseType.DUMBBELL)
                        WeightSpecification.TOTAL_WEIGHT
                    else
                        bit.variation.weightSpec

                val weight = bit.weight.calculate(
                    weightSpec,
                    bit.variation.bar)

                binding.weight.bigDecimal = weight.getValue(internationalSystem)
                binding.reps.text = bit.reps.toString()
                binding.note.text = bit.note

                if (bit.instant) {
                    binding.time.setText(R.string.symbol_empty)
                    setAlpha(binding, 0.4f)
                } else {
                    binding.time.text = bit.timestamp.getTimeString()
                    setAlpha(binding, 1f)
                }
                binding.number.visibility = View.GONE

                binding.root.setOnClickListener {
                    onClickListener?.accept(bit, index)
                }
            }
        }
    }

    private fun setAlpha(binding: ListitemHistoryBitBinding, alpha: Float) {
        listOf(binding.weight, binding.reps, binding.note, binding.number)
            .forEach { it.alpha = alpha }
    }
}