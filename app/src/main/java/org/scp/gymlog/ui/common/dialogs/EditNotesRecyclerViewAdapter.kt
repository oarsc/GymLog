package org.scp.gymlog.ui.common.dialogs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.scp.gymlog.databinding.ListitemSelectableTextBinding

class EditNotesRecyclerViewAdapter(
    private val values: List<String>,
    preSelected: List<String> = emptyList(),
    private val onClick: (String, Boolean) -> Unit
) : RecyclerView.Adapter<EditNotesRecyclerViewAdapter.ViewHolder>() {

    private val initialSelects = preSelected.toMutableList()

    private val clearActions = mutableListOf<Runnable>()
    fun clearSelects() = clearActions.forEach { it.run() }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ListitemSelectableTextBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val text = values[position]
        holder.mContentView.text = text
        holder.check = initialSelects.contains(text)
    }

    override fun getItemCount(): Int {
        return values.size
    }

    inner class ViewHolder(
        binding: ListitemSelectableTextBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        val mContentView: TextView = binding.content
        private val mCheck = binding.check

        var check: Boolean
            get() = mCheck.visibility == View.VISIBLE
            set(value) { mCheck.visibility = if(value) View.VISIBLE else View.INVISIBLE }

        init {
            clearActions.add {
                if (check) check = false
            }

            itemView.setOnClickListener {
                val value = !check
                val text = mContentView.text.toString()
                check = value
                onClick(text, value)
                if (!value) {
                    initialSelects.remove(text)
                }
            }
        }

        override fun toString(): String {
            return super.toString() + " '" + mContentView.text + "'"
        }
    }
}