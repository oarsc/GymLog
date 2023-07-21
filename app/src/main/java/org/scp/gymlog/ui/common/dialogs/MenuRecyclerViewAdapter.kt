package org.scp.gymlog.ui.common.dialogs

import android.content.Context
import android.view.LayoutInflater
import android.view.Menu
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.annotation.MenuRes
import androidx.recyclerview.widget.RecyclerView
import org.scp.gymlog.databinding.ListitemDialogMenuBinding
import java.util.function.Consumer

class MenuRecyclerViewAdapter(
    context: Context,
    @MenuRes menuId: Int,
    private val onClick: Consumer<Int>
) : RecyclerView.Adapter<MenuRecyclerViewAdapter.ViewHolder>() {

    private val menu: Menu

    init {
        val popupMenu = PopupMenu(context, null)
        popupMenu.inflate(menuId)
        menu = popupMenu.menu
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ListitemDialogMenuBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val element = menu.getItem(position)
        holder.mContentView.text = element.title
        holder.mImageView.setImageDrawable(element.icon)
        holder.id = element.itemId
    }

    override fun getItemCount(): Int {
        return menu.size()
    }

    inner class ViewHolder(binding: ListitemDialogMenuBinding) :
        RecyclerView.ViewHolder(binding.root) {

        val mContentView: TextView = binding.content
        val mImageView: ImageView = binding.image
        var id = 0

        init {
            itemView.setOnClickListener { onClick.accept(id) }
        }

        override fun toString(): String {
            return super.toString() + " '" + mContentView.text + "'"
        }
    }
}
