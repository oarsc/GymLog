package org.scp.gymlog.ui.main.history

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import org.scp.gymlog.R
import org.scp.gymlog.databinding.ListitemTrainingBinding
import org.scp.gymlog.model.Muscle
import org.scp.gymlog.ui.common.components.listView.CommonListView
import org.scp.gymlog.ui.common.components.listView.SimpleListHandler
import org.scp.gymlog.ui.training.TrainingActivity
import org.scp.gymlog.util.DateUtils.getTimeString
import org.scp.gymlog.util.DateUtils.minutesToTimeString

class HistoryTrainingListHandler(
	val context: Context
) : SimpleListHandler<TrainingData, ListitemTrainingBinding> {
	override val useListState = false
	override val itemInflater: (LayoutInflater, ViewGroup?, Boolean) -> ListitemTrainingBinding
		= ListitemTrainingBinding::inflate

	override fun buildListView(
		binding: ListitemTrainingBinding,
		item: TrainingData,
		index: Int,
		state: CommonListView.ListElementState?
	) {
		//holder.id = item.id
		binding.title.text = if (item.duration == null)
			String.format(
				context.getString(R.string.compound_training_start_date),
				item.id,
				item.startTime.getTimeString()
			)
		else
			String.format(
				context.getString(R.string.compound_training_duration),
				item.id,
				item.duration?.minutesToTimeString()
			)

		binding.subtitle.text = item.mostUsedMuscles
			.map(Muscle::text)
			.map { resText -> context.resources.getString(resText) }
			.joinToString { it }

		binding.indicator.setBackgroundResource(item.mostUsedMuscles[0].color)

		binding.root.setOnClickListener {
			val intent = Intent(context, TrainingActivity::class.java)
			intent.putExtra("trainingId", item.id)
			context.startActivity(intent)
		}
	}
}