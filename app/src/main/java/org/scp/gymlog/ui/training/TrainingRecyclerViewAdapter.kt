package org.scp.gymlog.ui.training

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.scp.gymlog.R
import org.scp.gymlog.databinding.ListElementFragmentHistoryBitBinding
import org.scp.gymlog.databinding.ListElementFragmentHistoryHeadersBinding
import org.scp.gymlog.databinding.ListElementFragmentHistoryVariationBinding
import org.scp.gymlog.model.Bit
import org.scp.gymlog.ui.training.rows.ITrainingRow
import org.scp.gymlog.ui.training.rows.TrainingBitRow
import org.scp.gymlog.ui.training.rows.TrainingVariationRow
import org.scp.gymlog.util.DateUtils.getTimeString
import org.scp.gymlog.util.FormatUtils.bigDecimal
import org.scp.gymlog.util.WeightUtils.calculate
import java.util.function.BiConsumer


class TrainingRecyclerViewAdapter(
    private val rows: ExerciseRows,
    private val internationalSystem: Boolean
) : RecyclerView.Adapter<TrainingRecyclerViewAdapter.ViewHolder>() {

    var onClickListener: BiConsumer<Bit, Int>? = null

    override fun getItemViewType(position: Int): Int {
        return when (rows[position].type) {
            ITrainingRow.Type.BIT -> R.layout.list_element_fragment_history_bit
            ITrainingRow.Type.VARIATION -> R.layout.list_element_fragment_history_variation
            else -> R.layout.list_element_fragment_history_headers
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when(viewType) {
            R.layout.list_element_fragment_history_bit -> ViewHolder(
                ListElementFragmentHistoryBitBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
            R.layout.list_element_fragment_history_variation -> ViewHolder(
                ListElementFragmentHistoryVariationBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
            else -> ViewHolder(
                ListElementFragmentHistoryHeadersBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val row = rows[position]

        when (row.type) {
            ITrainingRow.Type.HEADER -> {

            }
            ITrainingRow.Type.VARIATION -> {
                val vRow = row as TrainingVariationRow
                val variation = vRow.variation
                if (variation == null) {
                    holder.mNote!!.setText(R.string.text_default)
                } else {
                    holder.mNote!!.text = vRow.variation.name
                }
            }
            ITrainingRow.Type.BIT -> {
                holder.bitRow = row as TrainingBitRow
                val bit = holder.bitRow!!.bit

                val weight = bit.weight.calculate(
                    bit.variation.weightSpec,
                    bit.variation.bar)

                holder.mWeight!!.bigDecimal = weight.getValue(internationalSystem)
                holder.mReps!!.text = bit.reps.toString()
                holder.mNote!!.text = bit.note

                if (bit.instant) {
                    holder.mTime!!.setText(R.string.symbol_empty)
                    setAlpha(holder, 0.4f)
                } else {
                    holder.mTime!!.text = bit.timestamp.getTimeString()
                    setAlpha(holder, 1f)
                }
            }
        }
    }

    private fun setAlpha(holder: ViewHolder, alpha: Float) {
        listOf(holder.mWeight, holder.mReps, holder.mNote)
            .forEach { view -> view?.alpha = alpha }
    }

    override fun getItemCount(): Int {
        return rows.size
    }

    inner class ViewHolder : RecyclerView.ViewHolder {
        var bitRow: TrainingBitRow? = null
        val mWeight: TextView?
        val mReps: TextView?
        val mTime: TextView?
        val mNote: TextView?

        constructor(binding: ListElementFragmentHistoryBitBinding) : super(binding.root) {
            mWeight = binding.weight
            mReps = binding.reps
            mTime = binding.time
            mNote = binding.note

            itemView.setOnClickListener {
                onClickListener?.accept(bitRow!!.bit, rows.indexOf(bitRow!!))
            }
        }

        constructor(binding: ListElementFragmentHistoryVariationBinding) : super(binding.root) {
            mTime = null
            mReps = null
            mWeight = null
            mNote = binding.variationName
        }

        constructor(binding: ListElementFragmentHistoryHeadersBinding) : super(binding.root) {
            mNote = null
            mTime = null
            mReps = null
            mWeight = null
        }

        override fun toString(): String {
            return super.toString() + " '${mWeight!!.text}'"
        }
    }
}