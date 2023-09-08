package org.scp.gymlog.ui.common.components.listView

import android.view.LayoutInflater
import android.view.ViewGroup
import org.scp.gymlog.ui.common.components.listView.CommonListView.ListElementState

interface SimpleListHandler<T, B> {
    val useListState: Boolean
    val itemInflater: (LayoutInflater, ViewGroup?, Boolean) -> B

    fun buildListView(binding: B, item: T, index: Int, state: ListElementState?)
}