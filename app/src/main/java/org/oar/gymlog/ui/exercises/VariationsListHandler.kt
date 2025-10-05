package org.oar.gymlog.ui.exercises

import android.view.LayoutInflater
import android.view.ViewGroup
import org.oar.gymlog.databinding.ListitemExercisesVariationBinding
import org.oar.gymlog.model.Variation
import org.oar.gymlog.ui.common.components.listView.CommonListView.ListElementState
import org.oar.gymlog.ui.common.components.listView.SimpleListHandler
import java.util.function.Consumer

object VariationsListHandler: SimpleListHandler<Variation, ListitemExercisesVariationBinding> {

    private var onVariationClickListener: Consumer<Variation>? = null
    fun onVariationClicked(listener: Consumer<Variation>) {
        onVariationClickListener = listener
    }

    override val useListState = false
    override val itemInflater: (LayoutInflater, ViewGroup?, Boolean) -> ListitemExercisesVariationBinding
        = ListitemExercisesVariationBinding::inflate

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