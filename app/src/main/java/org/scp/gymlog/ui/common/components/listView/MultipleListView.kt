package org.scp.gymlog.ui.common.components.listView

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding

class MultipleListView<T: Any>(
    context: Context,
    attrs: AttributeSet? = null,
) : SimpleListView<T, ViewBinding>(context, attrs) {

    private lateinit var handler: MultipleListHandler<T>

    override fun init(
        listData: List<T>,
        handler: SimpleListHandler<T, ViewBinding>
    ) {
        throw java.lang.RuntimeException("Not supported for MultipleListView")
    }

    fun init(
        listData: List<T>,
        handler: MultipleListHandler<T>
    ) {
        this.data = listData.toMutableList()
        this.order = listData.indices.toMutableList()
        this.handler = handler
        this.states = mutableMapOf()
        adapter = CustomMultipleAdapter()
    }

    private inner class CustomMultipleAdapter: Adapter<CustomViewHolder>() {

        override fun getItemViewType(position: Int) = position
        override fun onCreateViewHolder(parent: ViewGroup, position: Int): CustomViewHolder {
            val inflater = handler.generateListItemInflater(data[position])
            return CustomViewHolder(
                inflater(LayoutInflater.from(parent.context), parent, false)
            )
        }

        override fun getItemCount() = data.size

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

    override fun applyToAll(callback : (ViewBinding?, T, ListElementState?) -> Unit) {
        val bindings = (0 until childCount)
            .map { getChildViewHolder(getChildAt(it)) }
            .map { it as SimpleListView<T, ViewBinding>.CustomViewHolder }
            .associate { it.item to it.binding }
            .toMutableMap()

        data.forEach { item ->
            val state = if (handler.useListState)
                states[item] ?: ListElementState().also { s -> states[item] = s } else
                null

            callback(bindings[item], item, state)
        }
    }
}