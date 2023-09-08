package org.scp.gymlog.ui.create

import android.view.LayoutInflater
import android.view.ViewGroup
import org.scp.gymlog.databinding.ListitemDefaultRowBinding
import org.scp.gymlog.ui.common.components.listView.CommonListView.ListElementState
import org.scp.gymlog.ui.common.components.listView.SimpleListHandler

object CreateExerciseListHandler: SimpleListHandler<CreateFormElement, ListitemDefaultRowBinding> {
    override val useListState = false
    override val itemInflater: (LayoutInflater, ViewGroup?, Boolean) -> ListitemDefaultRowBinding
        = ListitemDefaultRowBinding::inflate

    override fun buildListView(
        binding: ListitemDefaultRowBinding,
        item: CreateFormElement,
        index: Int,
        state: ListElementState?
    ) {
        binding.title.setText(item.title)
        binding.root.setOnClickListener { item.onClick(it) }

        updateValue(binding, item)
        item.onUpdateListener { updateValue(binding, item) }
    }

    private fun updateValue(binding: ListitemDefaultRowBinding, element: CreateFormElement) {
        binding.image.setImageDrawable(element.drawable)

        when {
            element.valueStr.isNotEmpty() -> binding.content.text = element.valueStr
            element.value == 0 -> binding.content.text = "-"
            else -> binding.content.setText(element.value)
        }
    }
}