package org.oar.gymlog.ui.registry

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.R.attr
import org.oar.gymlog.R
import org.oar.gymlog.databinding.ListitemLogBinding
import org.oar.gymlog.model.Bit
import org.oar.gymlog.ui.common.components.listView.CommonListView
import org.oar.gymlog.ui.common.components.listView.SimpleListHandler
import org.oar.gymlog.util.Constants
import org.oar.gymlog.util.Constants.TODAY
import org.oar.gymlog.util.Data
import org.oar.gymlog.util.DateUtils.diffYearsAndDays
import org.oar.gymlog.util.DateUtils.getLetterFrom
import org.oar.gymlog.util.DateUtils.getTimeString
import org.oar.gymlog.util.FormatUtils.bigDecimal
import org.oar.gymlog.util.FormatUtils.integer
import org.oar.gymlog.util.WeightUtils.calculate
import org.oar.gymlog.util.extensions.CommonExts.getThemeColor
import java.util.function.BiConsumer

class LogListHandler(
    val context: Context,
    private val log: List<Bit>,
    private val internationalSystem: Boolean
) : SimpleListHandler<Bit, ListitemLogBinding> {
    override val useListState = false

    var fullyLoaded = false
    private val defaultDayColor = context.getThemeColor(attr.colorSecondary)

    private var onLoadMoreListener: Runnable? = null
    fun setOnLoadMoreListener(onLoadMoreListener: Runnable) {
        this.onLoadMoreListener = onLoadMoreListener
    }

    private var onBitClickListener: BiConsumer<View, Bit>? = null
    fun setOnBitClickListener(onBitClickListener: BiConsumer<View, Bit>) {
        this.onBitClickListener = onBitClickListener
    }

    override val itemInflater: (LayoutInflater, ViewGroup?, Boolean) -> ListitemLogBinding
        = ListitemLogBinding::inflate

    override fun buildListView(
        binding: ListitemLogBinding,
        item: Bit,
        index: Int,
        state: CommonListView.ListElementState?
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

        if (item.trainingId == Data.training?.id) {
            if (item.instant) {
                binding.day.apply {
                    setText(R.string.symbol_empty)
                    setTextColor(defaultDayColor)
                }
                item.set = lastSet
            } else {
                binding.day.apply {
                    text = item.timestamp.getTimeString()
                    setTextColor(context.getColor(R.color.green))
                }
                item.set = lastSet + 1
            }
        } else {
            binding.day.setTextColor(defaultDayColor)

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