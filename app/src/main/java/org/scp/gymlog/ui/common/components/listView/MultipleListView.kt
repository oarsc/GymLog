package org.scp.gymlog.ui.common.components.listView

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding

class MultipleListView<T: Any>(
    context: Context,
    attrs: AttributeSet? = null,
) : CommonListView<T, ViewBinding>(context, attrs) {

    lateinit var handler: MultipleListHandler<T>
        private set

    fun init(
        listData: List<T>,
        handler: MultipleListHandler<T>
    ) {
        super.init(listData)
        this.handler = handler
        this.useState = handler.useListState

        adapter = CustomMultipleAdapter()
    }

    private inner class CustomMultipleAdapter: Adapter<CustomViewHolder>() {
        override fun getItemViewType(position: Int) = handler.findItemInflaterIndex(data[position])
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
            val inflater = handler.itemInflaters[viewType]
            return CustomViewHolder(
                inflater(LayoutInflater.from(parent.context), parent, false)
            )
        }

        override fun getItemCount() = size

        override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
            val index = lastComparatorUsed?.let { order[position] } ?: position
            val item = data[index]
            val state = if (handler.useListState)
                states[item] ?: ListElementState().also { states[item] = it } else
                null
            holder.item = item
            handler.buildListView(holder.binding, item, index, state)
        }
    }
}