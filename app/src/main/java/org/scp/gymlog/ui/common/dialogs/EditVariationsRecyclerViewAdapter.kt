package org.scp.gymlog.ui.common.dialogs

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.scp.gymlog.databinding.ListElementFragmentTextBinding
import org.scp.gymlog.model.Variation
import java.util.function.BiConsumer

class EditVariationsRecyclerViewAdapter (
    private val variations: List<Variation>,
    private val onClick: BiConsumer<Int, String>
) : RecyclerView.Adapter<EditVariationsRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ListElementFragmentTextBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.variation = variations[position]
        holder.mContentView.text = holder.variation!!.name
    }

    override fun getItemCount(): Int {
        return variations.size
    }

    fun updateText(index: Int, text: String) {
        variations[index].name = text
        notifyItemChanged(index)
    }

    inner class ViewHolder(binding: ListElementFragmentTextBinding) :
        RecyclerView.ViewHolder(binding.root) {

        val mContentView: TextView = binding.content
        var variation: Variation? = null

        init {
            itemView.setOnClickListener {
                val name = mContentView.text.toString()
                val index = variations.indexOf(variation)
                onClick.accept(index, name)
            }
        }

        override fun toString(): String {
            return super.toString() + " '" + mContentView.text + "'"
        }
    }
}