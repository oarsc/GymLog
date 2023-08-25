package org.scp.gymlog.ui.exercises

import android.view.LayoutInflater
import android.view.ViewGroup
import org.scp.gymlog.databinding.ListitemExercisesVariationBinding
import org.scp.gymlog.model.Variation
import org.scp.gymlog.ui.common.components.listView.SimpleListHandler
import org.scp.gymlog.ui.common.components.listView.SimpleListView.ListElementState
import java.util.function.Consumer

object VariationsListHandler: SimpleListHandler<Variation, ListitemExercisesVariationBinding> {

    private var onVariationClickListener: Consumer<Variation>? = null
    fun onVariationClicked(listener: Consumer<Variation>) {
        onVariationClickListener = listener
    }

    override val useListState = false

    override fun generateListItemInflater(): (LayoutInflater, ViewGroup?, Boolean) -> org.scp.gymlog.databinding.ListitemExercisesVariationBinding {
        return org.scp.gymlog.databinding.ListitemExercisesVariationBinding::inflate
    }

    override fun buildListView(
        binding: ListitemExercisesVariationBinding,
        item: Variation,
        index: Int,
        state: ListElementState?
    ) {
        binding.content.text = item.name
        onVariationClickListener?.also { listener ->
            binding.root.setOnClickListener { listener.accept(item) }
        }
    }
}