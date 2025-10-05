package org.oar.gymlog.ui.main.history

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import org.oar.gymlog.databinding.ListitemLegendBinding
import org.oar.gymlog.model.Muscle
import org.oar.gymlog.ui.common.components.listView.CommonListView
import org.oar.gymlog.ui.common.components.listView.SimpleListHandler

class HistoryLegendListHandler(
	val context: Context
) : SimpleListHandler<Muscle, ListitemLegendBinding> {
	override val useListState = false
	override val itemInflater: (LayoutInflater, ViewGroup?, Boolean) -> ListitemLegendBinding
		= ListitemLegendBinding::inflate

	override fun buildListView(
		binding: ListitemLegendBinding,
		item: Muscle,
		index: Int,
		state: CommonListView.ListElementState?
	) {
		binding.text.setText(item.text)
		binding.indicator.setCardBackgroundColor(
			ResourcesCompat.getColor(context.resources, item.color, null)
		)
	}
}