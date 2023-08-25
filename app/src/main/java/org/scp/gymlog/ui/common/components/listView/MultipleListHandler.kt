package org.scp.gymlog.ui.common.components.listView

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import org.scp.gymlog.ui.common.components.listView.SimpleListView.ListElementState

interface MultipleListHandler<T> {
    val useListState: Boolean

    fun generateListItemInflater(item: T): (LayoutInflater, ViewGroup?, Boolean) -> ViewBinding
    fun buildListView(binding: ViewBinding, item: T, index: Int, state: ListElementState?)
}