package org.scp.gymlog.ui.common.dialogs

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.scp.gymlog.R
import org.scp.gymlog.databinding.ListElementFragmentDialogMenuBinding
import java.util.function.BiConsumer

class TextSelectRecyclerViewAdapter(
    private val texts: List<String>,
    private val selectedOption: Int = -1,
    private val onClick: BiConsumer<Int, String>,
) : RecyclerView.Adapter<TextSelectRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ListElementFragmentDialogMenuBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    @SuppressLint("RecyclerView")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.mContentView.text = texts[position]
        if (selectedOption < 0) {
            holder.mImageView.visibility = View.GONE
        } else {
            holder.mImageView.setImageResource(
                if (position == selectedOption) R.drawable.ic_tick_24dp
                else R.drawable.ic_empty)
        }
        holder.index = position
    }

    override fun getItemCount(): Int {
        return texts.size
    }

    inner class ViewHolder(binding: ListElementFragmentDialogMenuBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val mContentView: TextView = binding.content
        val mImageView: ImageView = binding.image
        var index = 0

        init {
            itemView.setOnClickListener { onClick.accept(index, texts[index]) }
        }

        override fun toString(): String {
            return super.toString() + " '" + mContentView.text + "'"
        }
    }
}