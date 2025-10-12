package org.oar.gymlog.ui.common.dialogs

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.oar.gymlog.databinding.ListitemTextBinding
import java.util.function.Consumer

class EditNotesRecyclerViewAdapter(
    private val values: List<String>,
    private val onClick: Consumer<String>
) : RecyclerView.Adapter<EditNotesRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ListitemTextBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.mContentView.text = values[position]
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(
        binding: ListitemTextBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        val mContentView: TextView = binding.content

        init {
            itemView.setOnClickListener { onClick.accept(mContentView.text.toString()) }
        }

        override fun toString(): String {
            return super.toString() + " '" + mContentView.text + "'"
        }
    }
}