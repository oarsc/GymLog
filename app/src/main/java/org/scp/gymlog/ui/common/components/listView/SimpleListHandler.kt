package org.scp.gymlog.ui.common.components.listView

import android.view.LayoutInflater
import android.view.ViewGroup
import org.scp.gymlog.ui.common.components.listView.SimpleListView.ListElementState

interface SimpleListHandler<T, B> {
    val useListState: Boolean

    fun generateListItemInflater(): (LayoutInflater, ViewGroup?, Boolean) -> B
    fun buildListView(binding: B, item: T, index: Int, state: ListElementState?)
}