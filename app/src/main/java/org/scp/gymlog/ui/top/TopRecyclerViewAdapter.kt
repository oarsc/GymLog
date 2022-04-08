package org.scp.gymlog.ui.top

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.scp.gymlog.R
import org.scp.gymlog.databinding.ListElementFragmentTopBitBinding
import org.scp.gymlog.databinding.ListElementFragmentTopHeadersBinding
import org.scp.gymlog.databinding.ListElementFragmentTopSpaceBinding
import org.scp.gymlog.databinding.ListElementFragmentTopVariationBinding
import org.scp.gymlog.model.Bit
import org.scp.gymlog.model.Exercise
import org.scp.gymlog.ui.top.rows.ITopRow
import org.scp.gymlog.ui.top.rows.TopBitRow
import org.scp.gymlog.ui.top.rows.TopVariationRow
import org.scp.gymlog.util.DateUtils
import org.scp.gymlog.util.FormatUtils.toString
import org.scp.gymlog.util.WeightUtils.getWeightFromTotal
import java.util.*
import java.util.function.Consumer

class TopRecyclerViewAdapter(
    private val rows: List<ITopRow>,
    private val exercise: Exercise,
    private val internationalSystem: Boolean
) : RecyclerView.Adapter<TopRecyclerViewAdapter.ViewHolder>() {

    private val today: Calendar = Calendar.getInstance()
    var onClickListener: Consumer<Bit>? = null
    var onLongClickListener: Consumer<Bit>? = null

    override fun getItemViewType(position: Int): Int {
        return when (rows[position].type) {
            ITopRow.Type.BIT -> R.layout.list_element_fragment_top_bit
            ITopRow.Type.VARIATION -> R.layout.list_element_fragment_top_variation
            ITopRow.Type.HEADER -> R.layout.list_element_fragment_top_headers
            else -> R.layout.list_element_fragment_top_space
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if (viewType == R.layout.list_element_fragment_top_bit) return ViewHolder(
            ListElementFragmentTopBitBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
        if (viewType == R.layout.list_element_fragment_top_variation) return ViewHolder(
            ListElementFragmentTopVariationBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
        return if (viewType == R.layout.list_element_fragment_top_headers) ViewHolder(
            ListElementFragmentTopHeadersBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        ) else ViewHolder(
            ListElementFragmentTopSpaceBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val row = rows[position]

        if (row.type === ITopRow.Type.HEADER || row.type === ITopRow.Type.SPACE) {
            return
        }

        if (row.type === ITopRow.Type.VARIATION) {
            val vRow = row as TopVariationRow
            holder.mNote!!.text = vRow.variation.name
            return
        }

        val bRow = row as TopBitRow
        holder.topBit = bRow.bit

        val topBit = holder.topBit
        val weight = getWeightFromTotal(
            topBit!!.weight,
            exercise.weightSpec,
            exercise.bar,
            internationalSystem
        )

        holder.mWeight!!.text = toString(weight)
        holder.mReps!!.text = topBit.reps.toString()
        holder.mTime!!.text = DateUtils.getDate(topBit.timestamp) + " (" +
                DateUtils.calculateTimeLetter(topBit.timestamp, today) + ")"

        holder.mNote!!.text = topBit.note
    }

    override fun getItemCount(): Int {
        return rows.size
    }

    inner class ViewHolder : RecyclerView.ViewHolder {
        var topBit: Bit? = null
        val mWeight: TextView?
        val mReps: TextView?
        val mTime: TextView?
        val mNote: TextView?

        constructor(binding: ListElementFragmentTopBitBinding) : super(binding.root) {
            mWeight = binding.weight
            mReps = binding.reps
            mTime = binding.time
            mNote = binding.note
            itemView.setOnClickListener {
                onClickListener?.accept(topBit!!)
            }

            itemView.setOnLongClickListener {
                onLongClickListener?.accept(topBit!!)
                true
            }
        }

        constructor(binding: ListElementFragmentTopVariationBinding) : super(binding.root) {
            mTime = null
            mReps = null
            mWeight = null
            mNote = binding.variationName
        }

        constructor(binding: ListElementFragmentTopHeadersBinding) : super(binding.root) {
            mNote = null
            mTime = null
            mReps = null
            mWeight = null
        }

        constructor(binding: ListElementFragmentTopSpaceBinding) : super(binding.root) {
            mNote = null
            mTime = null
            mReps = null
            mWeight = null
        }
    }

}