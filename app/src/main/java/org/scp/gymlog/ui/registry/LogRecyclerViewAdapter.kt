package org.scp.gymlog.ui.registry

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.scp.gymlog.R
import org.scp.gymlog.databinding.ListitemLogBinding
import org.scp.gymlog.databinding.ListitemLogMoreButtonBinding
import org.scp.gymlog.model.Bit
import org.scp.gymlog.util.Constants
import org.scp.gymlog.util.DateUtils.currentDateTime
import org.scp.gymlog.util.DateUtils.diffYearsAndDays
import org.scp.gymlog.util.DateUtils.getLetterFrom
import org.scp.gymlog.util.DateUtils.getTimeString
import org.scp.gymlog.util.FormatUtils.bigDecimal
import org.scp.gymlog.util.FormatUtils.integer
import org.scp.gymlog.util.WeightUtils.calculate
import java.util.function.BiConsumer

class LogRecyclerViewAdapter(
    private val log: List<Bit>,
    private val currentTrainingId: Int?,
    private val internationalSystem: Boolean
) : RecyclerView.Adapter<LogRecyclerViewAdapter.ViewHolder>() {

    private val today = currentDateTime()
    var onClickElementListener: BiConsumer<View, Bit>? = null
    var onLoadMoreListener: Runnable? = null
    private var fullyLoaded = false

    fun setFullyLoaded(fullyLoaded: Boolean) {
        if (this.fullyLoaded != fullyLoaded) {
            this.fullyLoaded = fullyLoaded
            if (fullyLoaded) notifyItemRemoved(log.size) else notifyItemInserted(log.size)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == log.size)
            R.layout.listitem_log_more_button
        else
            R.layout.listitem_log
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when(viewType) {
            R.layout.listitem_log_more_button -> ViewHolder(
                ListitemLogMoreButtonBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
            else -> ViewHolder(
                ListitemLogBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder.loadMoreBtn) {
            return
        }
        holder.bit = log[position]
        val bit = holder.bit
        var lastSet = 0
        var lastDate = Constants.DATE_ZERO
        var lastTrainingId = -1
        if (position > 0) {
            val lastBit = log[position - 1]
            lastSet = lastBit.set
            lastDate = lastBit.timestamp
            lastTrainingId = lastBit.trainingId
        }

        if (bit.trainingId == currentTrainingId) {
            if (bit.instant) {
                holder.mDay!!.setText(R.string.symbol_empty)
                bit.set = lastSet
            } else {
                holder.mDay!!.text = bit.timestamp.getTimeString()
                bit.set = lastSet + 1
            }
        } else {
            val lastDateDiff = lastDate.diffYearsAndDays(bit.timestamp)

            if (lastDateDiff.years != 0 || lastDateDiff.days != 0) {
                val dayLabel = today.getLetterFrom(bit.timestamp)
                holder.mDay!!.text = dayLabel
                bit.set = 1
            } else {
                holder.mDay!!.setText(R.string.symbol_empty)
                if (lastTrainingId == bit.trainingId) bit.set =
                    if (bit.instant) lastSet else lastSet + 1 else bit.set = 1
            }
        }

        val weight = bit.weight.calculate(
            bit.variation.weightSpec,
            bit.variation.bar)

        holder.mWeight!!.bigDecimal = weight.getValue(internationalSystem)
        if (bit.instant) {
            holder.mSet!!.setText(R.string.symbol_empty)
            setAlpha(holder, 0.4f)
        } else {
            holder.mSet!!.integer = bit.set
            setAlpha(holder, 1f)
        }

        holder.mReps!!.integer = bit.reps
        holder.mNotes!!.text = bit.note
        holder.element!!.setPadding(0, 0, 0, 0)
    }

    private fun setAlpha(holder: ViewHolder, alpha: Float) {
        listOf(holder.mWeight, holder.mReps, holder.mNotes)
            .forEach { v -> v?.alpha = alpha }
    }

    fun notifyTrainingIdChanged(trainingId: Int, preIndex: Int) {
        var startIndex = 0
        var numberOfElements = 0

        var found = false
        for ((idx, bitLog) in log.withIndex()) {
            if (bitLog.trainingId == trainingId) {
                if (!found) {
                    startIndex = idx
                    found = true
                }
                numberOfElements++
            } else if (found) break
        }

        if (numberOfElements > 0) {
            if (preIndex > startIndex && preIndex < startIndex + numberOfElements)
                notifyItemRangeChanged(preIndex, numberOfElements + startIndex - preIndex)
            else
                notifyItemRangeChanged(startIndex, numberOfElements)
        }
    }

    override fun getItemCount(): Int {
        return if (fullyLoaded) log.size else log.size + 1 // + load more button
    }

    inner class ViewHolder : RecyclerView.ViewHolder {
        lateinit var bit: Bit
        var mDay: TextView? = null
        var mSet: TextView? = null
        var mWeight: TextView? = null
        var mReps: TextView? = null
        var mNotes: TextView? = null
        var element: LinearLayout? = null
        var set = 0
        var loadMoreBtn = false

        constructor(binding: ListitemLogBinding) : super(binding.root) {
            mDay = binding.day
            mSet = binding.set
            mWeight = binding.weight
            mReps = binding.reps
            mNotes = binding.notes
            element = binding.element
            itemView.setOnClickListener { onClickElementListener?.accept(itemView, bit) }
        }

        constructor(binding: ListitemLogMoreButtonBinding) : super(binding.root) {
            loadMoreBtn = true
            itemView.setOnClickListener { onLoadMoreListener?.run() }
        }

        override fun toString(): String {
            return super.toString() + " '" + mDay!!.text + "'"
        }
    }
}