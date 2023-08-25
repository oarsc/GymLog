package org.scp.gymlog.ui.main.history

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import org.scp.gymlog.databinding.ListitemLegendBinding
import org.scp.gymlog.model.Muscle
import org.scp.gymlog.ui.common.components.listView.SimpleListHandler
import org.scp.gymlog.ui.common.components.listView.SimpleListView

class HistoryLegendListHandler(
	val context: Context
) : SimpleListHandler<Muscle, ListitemLegendBinding> {
	override val useListState = false

	override fun generateListItemInflater(): (LayoutInflater, ViewGroup?, Boolean) -> ListitemLegendBinding {
		return ListitemLegendBinding::inflate
	}

	override fun buildListView(
		binding: ListitemLegendBinding,
		item: Muscle,
		index: Int,
		state: SimpleListView.ListElementState?
	) {
		binding.text.setText(item.text)
		binding.indicator.setCardBackgroundColor(
			ResourcesCompat.getColor(context.resources, item.color, null)
		)
	}
}