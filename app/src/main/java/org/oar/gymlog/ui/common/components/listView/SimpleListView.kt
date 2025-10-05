package org.oar.gymlog.ui.common.components.listView

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding

open class SimpleListView<T: Any, B: ViewBinding>(
    context: Context,
    attrs: AttributeSet? = null
) : CommonListView<T, B>(context, attrs) {

    lateinit var handler: SimpleListHandler<T, B>
        private set

    fun init(
        listData: List<T>,
        handler: SimpleListHandler<T, B>
    ) {
        super.init(listData)
        this.handler = handler
        this.useState = handler.useListState

        adapter = CustomSimpleAdapter()
    }

    private inner class CustomSimpleAdapter: Adapter<CustomViewHolder<T, B>>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder<T, B> {
            return CustomViewHolder(
                handler.itemInflater(LayoutInflater.from(parent.context), parent, false)
            )
        }

        override fun getItemCount() = size

        override fun onBindViewHolder(holder: CustomViewHolder<T, B>, position: Int) {
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