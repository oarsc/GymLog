package org.scp.gymlog.ui.registry

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import org.scp.gymlog.R
import org.scp.gymlog.databinding.ListitemLogBinding
import org.scp.gymlog.databinding.ListitemLogMoreButtonBinding
import org.scp.gymlog.model.Bit
import org.scp.gymlog.ui.common.components.listView.MultipleListHandler
import org.scp.gymlog.ui.common.components.listView.SimpleListHandler
import org.scp.gymlog.ui.common.components.listView.SimpleListView
import org.scp.gymlog.util.Constants
import org.scp.gymlog.util.Constants.TODAY
import org.scp.gymlog.util.Data
import org.scp.gymlog.util.DateUtils.diffYearsAndDays
import org.scp.gymlog.util.DateUtils.getLetterFrom
import org.scp.gymlog.util.DateUtils.getTimeString
import org.scp.gymlog.util.FormatUtils.bigDecimal
import org.scp.gymlog.util.FormatUtils.integer
import org.scp.gymlog.util.WeightUtils.calculate
import java.util.function.BiConsumer

class LogListHandler(
    private val log: List<Bit>,
    private val internationalSystem: Boolean
) : SimpleListHandler<Bit, ListitemLogBinding> {
    override val useListState = false

    var fullyLoaded = false

    private var onLoadMoreListener: Runnable? = null
    fun setOnLoadMoreListener(onLoadMoreListener: Runnable) {
        this.onLoadMoreListener = onLoadMoreListener
    }

    private var onBitClickListener: BiConsumer<View, Bit>? = null
    fun setOnBitClickListener(onBitClickListener: BiConsumer<View, Bit>) {
        this.onBitClickListener = onBitClickListener
    }

    override fun generateListItemInflater(): (LayoutInflater, ViewGroup?, Boolean) -> ListitemLogBinding {
        return ListitemLogBinding::inflate
    }

    override fun buildListView(
        binding: ListitemLogBinding,
        item: Bit,
        index: Int,
        state: SimpleListView.ListElementState?
    ) {
        var lastSet = 0
        var lastDate = Constants.DATE_ZERO
        var lastTrainingId = -1
        if (index > 0) {
            val lastBit = log[index - 1]
            lastSet = lastBit.set
            lastDate = lastBit.timestamp
            lastTrainingId = lastBit.trainingId
        }

        if (item.trainingId == Data.trainingId) {
            if (item.instant) {
                binding.day.setText(R.string.symbol_empty)
                item.set = lastSet
            } else {
                binding.day.text = item.timestamp.getTimeString()
                item.set = lastSet + 1
            }
        } else {
            val lastDateDiff = lastDate.diffYearsAndDays(item.timestamp)

            if (lastDateDiff.years != 0 || lastDateDiff.days != 0) {
                val dayLabel = TODAY.getLetterFrom(item.timestamp)
                binding.day.text = dayLabel
                item.set = 1
            } else {
                binding.day.setText(R.string.symbol_empty)
                if (lastTrainingId == item.trainingId) item.set =
                    if (item.instant) lastSet else lastSet + 1 else item.set = 1
            }
        }

        val weight = item.weight.calculate(
            item.variation.weightSpec,
            item.variation.bar)

        binding.weight.bigDecimal = weight.getValue(internationalSystem)
        if (item.instant) {
            binding.set.setText(R.string.symbol_empty)
            setAlpha(binding, 0.4f)
        } else {
            binding.set.integer = item.set
            setAlpha(binding, 1f)
        }

        binding.reps.integer = item.reps
        binding.notes.text = item.note

        //binding.element.setPadding(0, 0, 0, 0)
        //binding.root.setPadding(0, 0, 0, 0)

        binding.root.setOnClickListener {
            onBitClickListener?.accept(binding.root, item)
        }

        if (!fullyLoaded && index == log.size-1) {
            onLoadMoreListener?.run()
        }
    }

    private fun setAlpha(binding: ListitemLogBinding, alpha: Float) {
        listOf(binding.weight, binding.reps, binding.notes)
            .forEach { it.alpha = alpha }
    }
}