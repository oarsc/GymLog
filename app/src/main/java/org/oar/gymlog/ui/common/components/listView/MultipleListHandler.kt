package org.oar.gymlog.ui.common.components.listView

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import org.oar.gymlog.ui.common.components.listView.CommonListView.ListElementState

interface MultipleListHandler<T: Any> {
    val useListState: Boolean
    val itemInflaters: List<(LayoutInflater, ViewGroup?, Boolean) -> ViewBinding>

    fun findItemInflaterIndex(item: T): Int
    fun buildListView(binding: ViewBinding, item: T, index: Int, state: ListElementState?)
}