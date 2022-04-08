package org.scp.gymlog.ui.registry

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.scp.gymlog.R
import org.scp.gymlog.databinding.ListElementFragmentLogBinding
import org.scp.gymlog.databinding.ListElementFragmentLogMoreButtonBinding
import org.scp.gymlog.model.Bit
import org.scp.gymlog.model.Exercise
import org.scp.gymlog.util.Constants
import org.scp.gymlog.util.DateUtils
import org.scp.gymlog.util.FormatUtils
import org.scp.gymlog.util.WeightUtils
import java.util.*
import java.util.function.BiConsumer

class LogRecyclerViewAdapter(
    private val log: List<Bit>,
    private val exercise: Exercise,
    private val currentTrainingId: Int,
    private val internationalSystem: Boolean
) : RecyclerView.Adapter<LogRecyclerViewAdapter.ViewHolder>() {

    private val today: Calendar = Calendar.getInstance()
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
            R.layout.list_element_fragment_log_more_button
        else
            R.layout.list_element_fragment_log
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (viewType == R.layout.list_element_fragment_log_more_button) {
            ViewHolder(
                ListElementFragmentLogMoreButtonBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        } else {
            ViewHolder(
                ListElementFragmentLogBinding.inflate(
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

        if (bit!!.trainingId == currentTrainingId) {
            if (lastTrainingId == currentTrainingId) {
                holder.mDay!!.setText(R.string.symbol_empty)
                bit.set = if (bit.instant) lastSet else lastSet + 1
            } else {
                holder.mDay!!.text = "T"
                bit.set = 1
            }

        } else {
            val lastDateDiff = DateUtils.yearsAndDaysDiff(lastDate, bit.timestamp)

            if (lastDateDiff[0] != 0 || lastDateDiff[1] != 0) {
                val dayLabel = DateUtils.calculateTimeLetter(
                    bit.timestamp, today
                )
                holder.mDay!!.text = dayLabel
                bit.set = 1
            } else {
                holder.mDay!!.setText(R.string.symbol_empty)
                if (lastTrainingId == bit.trainingId) bit.set =
                    if (bit.instant) lastSet else lastSet + 1 else bit.set = 1
            }
        }

        val weight = WeightUtils.getWeightFromTotal(
            bit.weight,
            exercise.weightSpec,
            exercise.bar,
            internationalSystem)

        holder.mWeight!!.text = FormatUtils.toString(weight)
        if (bit.instant) {
            holder.mSet!!.setText(R.string.symbol_empty)
            setAlpha(holder, 0.4f)
        } else {
            holder.mSet!!.text = java.lang.String.valueOf(bit.set)
            setAlpha(holder, 1f)
        }

        holder.mReps!!.text = java.lang.String.valueOf(bit.reps)
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
        var bit: Bit? = null
        var mDay: TextView? = null
        var mSet: TextView? = null
        var mWeight: TextView? = null
        var mReps: TextView? = null
        var mNotes: TextView? = null
        var element: LinearLayout? = null
        var set = 0
        var loadMoreBtn = false

        constructor(binding: ListElementFragmentLogBinding) : super(binding.root) {
            mDay = binding.day
            mSet = binding.set
            mWeight = binding.weight
            mReps = binding.reps
            mNotes = binding.notes
            element = binding.element
            itemView.setOnClickListener { onClickElementListener?.accept(itemView, bit!!) }
        }

        constructor(binding: ListElementFragmentLogMoreButtonBinding) : super(binding.root) {
            loadMoreBtn = true
            itemView.setOnClickListener { onLoadMoreListener?.run() }
        }

        override fun toString(): String {
            return super.toString() + " '" + mDay!!.text + "'"
        }
    }
}