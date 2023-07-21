package org.scp.gymlog.ui.top

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.scp.gymlog.R
import org.scp.gymlog.databinding.ListitemTopBitBinding
import org.scp.gymlog.databinding.ListitemTopHeadersBinding
import org.scp.gymlog.databinding.ListitemTopSpaceBinding
import org.scp.gymlog.databinding.ListitemTopVariationBinding
import org.scp.gymlog.model.Bit
import org.scp.gymlog.ui.top.rows.ITopRow
import org.scp.gymlog.ui.top.rows.TopBitRow
import org.scp.gymlog.ui.top.rows.TopVariationRow
import org.scp.gymlog.util.DateUtils.getDateString
import org.scp.gymlog.util.DateUtils.getLetterFrom
import org.scp.gymlog.util.DateUtils.currentDateTime
import org.scp.gymlog.util.FormatUtils.bigDecimal
import org.scp.gymlog.util.FormatUtils.integer
import org.scp.gymlog.util.WeightUtils.calculate
import java.util.function.Consumer

class TopRecyclerViewAdapter(
    private val rows: List<ITopRow>,
    private val internationalSystem: Boolean
) : RecyclerView.Adapter<TopRecyclerViewAdapter.ViewHolder>() {

    private val today = currentDateTime()
    var onClickListener: Consumer<Bit>? = null
    var onLongClickListener: Consumer<Bit>? = null

    override fun getItemViewType(position: Int): Int {
        return when (rows[position].type) {
            ITopRow.Type.BIT -> R.layout.listitem_top_bit
            ITopRow.Type.VARIATION -> R.layout.listitem_top_variation
            ITopRow.Type.HEADER -> R.layout.listitem_top_headers
            else -> R.layout.listitem_top_space
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when(viewType) {
            R.layout.listitem_top_bit -> ViewHolder(
                ListitemTopBitBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
            R.layout.listitem_top_variation -> ViewHolder(
                ListitemTopVariationBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
            R.layout.listitem_top_headers -> ViewHolder(
                ListitemTopHeadersBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
            else -> ViewHolder(
                ListitemTopSpaceBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val row = rows[position]

        when (row.type) {
            ITopRow.Type.HEADER -> {

            }
            ITopRow.Type.SPACE -> {

            }
            ITopRow.Type.VARIATION -> {
                val vRow = row as TopVariationRow
                holder.mNote?.text = vRow.variation.name
            }
            ITopRow.Type.BIT -> {
                val bRow = row as TopBitRow
                holder.topBit = bRow.bit

                val topBit = holder.topBit
                val weight = topBit.weight.calculate(
                    bRow.bit.variation.weightSpec,
                    bRow.bit.variation.bar)

                holder.mWeight?.bigDecimal = weight.getValue(internationalSystem)
                holder.mReps?.integer = topBit.reps
                holder.mTime?.text = topBit.timestamp.getDateString() + " (" +
                        today.getLetterFrom(topBit.timestamp) + ")"

                holder.mNote?.text = topBit.note
            }
        }
    }

    override fun getItemCount(): Int {
        return rows.size
    }

    inner class ViewHolder : RecyclerView.ViewHolder {
        lateinit var topBit: Bit
        val mWeight: TextView?
        val mReps: TextView?
        val mTime: TextView?
        val mNote: TextView?

        constructor(binding: ListitemTopBitBinding) : super(binding.root) {
            mWeight = binding.weight
            mReps = binding.reps
            mTime = binding.time
            mNote = binding.note
            itemView.setOnClickListener {
                onClickListener?.accept(topBit)
            }

            itemView.setOnLongClickListener {
                onLongClickListener?.accept(topBit)
                true
            }
        }

        constructor(binding: ListitemTopVariationBinding) : super(binding.root) {
            mTime = null
            mReps = null
            mWeight = null
            mNote = binding.variationName
        }

        constructor(binding: ListitemTopHeadersBinding) : super(binding.root) {
            mNote = null
            mTime = null
            mReps = null
            mWeight = null
        }

        constructor(binding: ListitemTopSpaceBinding) : super(binding.root) {
            mNote = null
            mTime = null
            mReps = null
            mWeight = null
        }
    }

}