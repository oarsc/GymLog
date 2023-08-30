package org.scp.gymlog.ui.common.components.listView

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

open class SimpleListView<T: Any, B: ViewBinding>(
    context: Context,
    attrs: AttributeSet? = null
) : RecyclerView(context, attrs) {

    lateinit var data: MutableList<T>
        protected set
    protected lateinit var order: MutableList<Int>     // visualIndex -> original data index
    protected lateinit var states: MutableMap<T, ListElementState>
    private lateinit var handler: SimpleListHandler<T, B>

    protected var lastComparatorUsed: Comparator<T>? = null

    var unScrollableVertically: Boolean = false

    @Suppress("UNCHECKED_CAST")
    class ListElementState: HashMap<String, Any>() {
        operator fun <T: Any> get(key: String, def: T) = (this[key] as T?) ?: def
    }

    init {
        layoutManager = object : LinearLayoutManager(context) {
            override fun canScrollVertically() = if (unScrollableVertically) false
                else super.canScrollVertically()
        }

        itemAnimator = DefaultItemAnimator()
    }


    open fun init(
        listData: List<T>,
        handler: SimpleListHandler<T, B>
    ) {
        this.data = listData.toMutableList()
        this.order = listData.indices.toMutableList()
        this.handler = handler
        this.states = mutableMapOf()

        adapter = CustomSimpleAdapter()
    }

    private inner class CustomSimpleAdapter: Adapter<CustomViewHolder>() {
        val inflater by lazy { handler.generateListItemInflater() }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
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

    internal inner class CustomViewHolder(
        val binding: B,
    ): ViewHolder(binding.root) {
        var item: T? = null
    }

    val size: Int
        get() = data.size

    fun setListData(listData: List<T>) {
        this.data.clear()
        this.data.addAll(listData)
        this.order = listData.indices.toMutableList()
        forceReorder()
    }

    fun sort(comparator: Comparator<T>) {
        this.lastComparatorUsed = comparator

        val sorted = this.data.toMutableList().sortedWith(comparator)
        this.order = sorted.map { this.data.indexOf(it) }.toMutableList()

        notifyDataSetChanged()
    }

    fun forceReorder(): Boolean {
        return lastComparatorUsed
            ?.let { sort(it); true }
            ?: false
    }

    fun scrollToPosition(position: Int, offset: Int) =
        (layoutManager as LinearLayoutManager).scrollToPositionWithOffset(position, offset)

    fun remove(element: T) {
        val dataIndex = data.indexOf(element)
        if (dataIndex < 0) return
        removeAt(dataIndex)
    }

    fun removeAt(position: Int) {
        data.removeAt(position)
        val orderPosition = order.indexOf(position)
        order.removeAt(orderPosition)
        order.replaceAll { if (it > position) it - 1 else it }

        notifyItemRemoved(orderPosition)
    }

    fun add(element: T) {
        order.add(order.size)
        data.add(element)

        if (!forceReorder()) {
            notifyItemInserted(order.size - 1)
        }
    }

    fun add(elements: List<T>) {
        val originalSize = data.size

        order.addAll(originalSize until originalSize+elements.size)
        data.addAll(elements)

        if (!forceReorder()) {
            notifyItemRangeInserted(originalSize, elements.size)
        }
    }

    fun insert(position: Int, element: T) {
        data.add(position, element)
        order.replaceAll { if (it >= position) it + 1 else it }
        order.add(position, position)

        notifyItemInserted(position)
    }

    fun update(element: T, index: Int) {
        data.removeAt(index)
        data.add(index, element)

        val orderIndex = order.indexOf(index)
        notifyItemChanged(orderIndex)
    }

    fun notifyUpdate(element: T) {
        val dataIndex = data.indexOf(element)
        if (dataIndex >= 0) {
            val orderIndex = order.indexOf(dataIndex)
            notifyItemChanged(orderIndex)
        }
    }

    open fun applyToAll(callback : (B?, T, ListElementState?) -> Unit) {
        val bindings = (0 until childCount)
            .map { getChildViewHolder(getChildAt(it)) }
            .map { it as SimpleListView<T, B>.CustomViewHolder }
            .associate { it.item to it.binding }

        data.forEach { item ->
            val state = if (handler.useListState)
                states[item] ?: ListElementState().also { s -> states[item] = s } else
                null

            callback(bindings[item], item, state)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun notifyDataSetChanged() = adapter?.notifyDataSetChanged()
    private fun notifyItemInserted(position: Int) = adapter?.notifyItemInserted(position)
    private fun notifyItemRemoved(position: Int) = adapter?.notifyItemRemoved(position)
    fun notifyItemChanged(position: Int) = adapter?.notifyItemChanged(position)
    fun notifyItemRangeChanged(positionStart: Int, itemCount: Int) =
        adapter?.notifyItemRangeChanged(positionStart, itemCount)
    private fun notifyItemRangeRemoved(positionStart: Int, itemCount: Int) =
        adapter?.notifyItemRangeRemoved(positionStart, itemCount)
    private fun notifyItemRangeInserted(positionStart: Int, itemCount: Int) =
        adapter?.notifyItemRangeInserted(positionStart, itemCount)

    fun notifyAllSizeChanged() =
        adapter?.notifyItemRangeChanged(0, data.size)

    @SuppressLint("NotifyDataSetChanged")
    fun dynamicallyItemsChangedBySize(initialSize: Int, endSize: Int = data.size) {
        adapter ?: return

        if (initialSize == 0) {
            if (endSize != 0) {
                notifyItemInserted(endSize)
            }
        } else if (endSize == 0) {
            notifyItemRangeRemoved(0, initialSize)
        } else if (initialSize == endSize) {
            notifyItemRangeChanged(0, initialSize)
        } else {
            notifyDataSetChanged()
        }
    }
}