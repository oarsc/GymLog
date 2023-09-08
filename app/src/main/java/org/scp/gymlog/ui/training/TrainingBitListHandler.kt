package org.scp.gymlog.ui.training

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import org.scp.gymlog.R
import org.scp.gymlog.databinding.*
import org.scp.gymlog.model.Bit
import org.scp.gymlog.model.Variation
import org.scp.gymlog.ui.common.components.listView.CommonListView
import org.scp.gymlog.ui.common.components.listView.MultipleListHandler
import org.scp.gymlog.ui.training.rows.ITrainingRow
import org.scp.gymlog.ui.training.rows.TrainingBitRow
import org.scp.gymlog.ui.training.rows.TrainingVariationRow
import org.scp.gymlog.util.DateUtils.getTimeString
import org.scp.gymlog.util.FormatUtils.bigDecimal
import org.scp.gymlog.util.WeightUtils.calculate
import java.util.function.BiConsumer

class TrainingBitListHandler(
    private val internationalSystem: Boolean,
    private val superSetVariations: List<Variation> = listOf(),
) : MultipleListHandler<ITrainingRow> {
    override val useListState = false
    override val itemInflaters = listOf<(LayoutInflater, ViewGroup?, Boolean) -> ViewBinding>(
        ListitemHistoryHeadersBinding::inflate,
        ListitemHistoryBitBinding::inflate,
        ListitemHistoryVariationBinding::inflate
    )

    override fun findItemInflaterIndex(item: ITrainingRow) = when (item.type) {
        ITrainingRow.Type.HEADER,
        ITrainingRow.Type.HEADER_SUPERSET -> 0
        ITrainingRow.Type.BIT,
        ITrainingRow.Type.BIT_SUPERSET -> 1
        ITrainingRow.Type.VARIATION -> 2
    }

    private var onClickListener: BiConsumer<Bit, Int>? = null

    fun setOnClickListener(onClickListener: BiConsumer<Bit, Int>) {
        this.onClickListener = onClickListener
    }

    override fun buildListView(
        binding: ViewBinding,
        item: ITrainingRow,
        index: Int,
        state: CommonListView.ListElementState?
    ) {
        when (item.type) {
            ITrainingRow.Type.HEADER -> {
                binding as ListitemHistoryHeadersBinding
                binding.hashColumn.visibility = View.GONE
            }
            ITrainingRow.Type.HEADER_SUPERSET -> { }

            ITrainingRow.Type.VARIATION -> {
                binding as ListitemHistoryVariationBinding
                item as TrainingVariationRow
                binding.variationName.text = item.variation.name
            }

            ITrainingRow.Type.BIT -> {
                binding as ListitemHistoryBitBinding
                item as TrainingBitRow

                val bit = item.bit

                val weight = bit.weight.calculate(
                    bit.variation.weightSpec,
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
            ITrainingRow.Type.BIT_SUPERSET -> {
                binding as ListitemHistoryBitBinding
                item as TrainingBitRow

                val bit = item.bit

                val weight = bit.weight.calculate(
                    bit.variation.weightSpec,
                    bit.variation.bar)

                binding.weight.bigDecimal = weight.getValue(internationalSystem)
                binding.reps.text = bit.reps.toString()
                binding.note.text = bit.note

                if (bit.instant) {
                    binding.time.setText(R.string.symbol_empty)
                    setAlpha(binding, 0.4f)
                } else {
                    val index: Int = superSetVariations.indexOf(bit.variation) + 1
                    binding.number.text = index.toString()

                    binding.time.text = bit.timestamp.getTimeString()
                    if (index != 1) {
                        binding.time.alpha = 0.7f
                    } else {
                        setBold(binding)
                    }
                    setAlpha(binding, 1f)
                }

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

    private fun setBold(binding: ListitemHistoryBitBinding) {
        listOf(binding.weight, binding.reps, binding.note, binding.number, binding.time)
            .forEach { it.setTypeface(null, Typeface.BOLD) }
    }
}