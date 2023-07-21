package org.scp.gymlog.ui.training

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.scp.gymlog.R
import org.scp.gymlog.databinding.ListitemHistoryBitBinding
import org.scp.gymlog.databinding.ListitemHistoryHeadersBinding
import org.scp.gymlog.databinding.ListitemHistoryVariationBinding
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
            ITrainingRow.Type.HEADER,
            ITrainingRow.Type.HEADER_SUPERSET -> R.layout.listitem_history_headers
            ITrainingRow.Type.BIT,
            ITrainingRow.Type.BIT_SUPERSET -> R.layout.listitem_history_bit
            ITrainingRow.Type.VARIATION -> R.layout.listitem_history_variation
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when(viewType) {
            R.layout.listitem_history_bit -> ViewHolder(
                ListitemHistoryBitBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
            R.layout.listitem_history_variation -> ViewHolder(
                ListitemHistoryVariationBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
            else -> ViewHolder(
                ListitemHistoryHeadersBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val row = rows[position]

        when (row.type) {
            ITrainingRow.Type.HEADER -> {
                holder.mNumber!!.visibility = View.GONE
            }
            ITrainingRow.Type.HEADER_SUPERSET -> { }

            ITrainingRow.Type.VARIATION -> {
                val vRow = row as TrainingVariationRow
                holder.mNote!!.text = vRow.variation.name
            }

            ITrainingRow.Type.BIT -> {
                holder.bitRow = row as TrainingBitRow
                val bit = holder.bitRow!!.bit

                val weight = bit.weight.calculate(
                    bit.variation.weightSpec,
                    bit.variation.bar)

                holder.mNumber!!.visibility = View.GONE
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
            ITrainingRow.Type.BIT_SUPERSET -> {
                holder.bitRow = row as TrainingBitRow
                val bit = holder.bitRow!!.bit

                val weight = bit.weight.calculate(
                    bit.variation.weightSpec,
                    bit.variation.bar)

                holder.mWeight!!.bigDecimal = weight.getValue(internationalSystem)
                holder.mReps!!.text = bit.reps.toString()
                holder.mNote!!.text = bit.note

                val index = rows.variations.indexOf(bit.variation) + 1
                holder.mNumber!!.text = index.toString()

                if (bit.instant) {
                    holder.mTime!!.setText(R.string.symbol_empty)
                    setAlpha(holder, 0.4f)
                } else {
                    if (index == 1) {
                        holder.mTime!!.text = bit.timestamp.getTimeString()
                    } else {
                        holder.mTime!!.setText(R.string.symbol_empty)
                    }
                    setAlpha(holder, 1f)
                }
            }
        }
    }

    private fun setAlpha(holder: ViewHolder, alpha: Float) {
        listOf(holder.mWeight, holder.mReps, holder.mNote, holder.mNumber)
            .forEach { it?.alpha = alpha }
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
        val mNumber: TextView?

        constructor(binding: ListitemHistoryBitBinding) : super(binding.root) {
            mWeight = binding.weight
            mReps = binding.reps
            mTime = binding.time
            mNote = binding.note
            mNumber = binding.number

            itemView.setOnClickListener {
                onClickListener?.accept(bitRow!!.bit, rows.indexOf(bitRow!!))
            }
        }

        constructor(binding: ListitemHistoryVariationBinding) : super(binding.root) {
            mTime = null
            mReps = null
            mWeight = null
            mNote = binding.variationName
            mNumber = null
        }

        constructor(binding: ListitemHistoryHeadersBinding) : super(binding.root) {
            mNote = null
            mTime = null
            mReps = null
            mWeight = null
            mNumber = binding.hashColumn
        }

        override fun toString(): String {
            return super.toString() + " '${mWeight!!.text}'"
        }
    }
}