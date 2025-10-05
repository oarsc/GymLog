package org.oar.gymlog.ui.main.history

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oar.gymlog.R
import org.oar.gymlog.databinding.ListitemTrainingBinding
import org.oar.gymlog.model.Muscle
import org.oar.gymlog.model.Training
import org.oar.gymlog.ui.common.components.listView.CommonListView
import org.oar.gymlog.ui.common.components.listView.SimpleListHandler
import org.oar.gymlog.util.DateUtils.getTimeString
import org.oar.gymlog.util.DateUtils.minutesToTimeString
import java.util.function.Consumer

class HistoryTrainingListHandler(
	val context: Context
) : SimpleListHandler<TrainingData, ListitemTrainingBinding> {
	override val useListState = false
	override val itemInflater: (LayoutInflater, ViewGroup?, Boolean) -> ListitemTrainingBinding
		= ListitemTrainingBinding::inflate

	private var onClickListener: Consumer<Training>? = null

	fun setOnClickListener(onClickListener: Consumer<Training>) {
		this.onClickListener = onClickListener
	}

	override fun buildListView(
		binding: ListitemTrainingBinding,
		item: TrainingData,
		index: Int,
		state: CommonListView.ListElementState?
	) {
		val training = item.training
		binding.title.text = if (item.duration == null)
			String.format(
				context.getString(R.string.compound_training_start_date),
				training.id,
				training.start.getTimeString()
			)
		else
			String.format(
				context.getString(R.string.compound_training_duration),
				training.id,
				item.duration?.minutesToTimeString()
			)

		binding.notesImage.visibility = if (training.note.isEmpty()) View.GONE else View.VISIBLE

		binding.subtitle.text = item.mostUsedMuscles
			.map(Muscle::textShort)
			.map { resText -> context.resources.getString(resText) }
			.joinToString { it }

		binding.indicator.setBackgroundResource(item.mostUsedMuscles[0].color)

		onClickListener?.also { listener ->
			binding.root.setOnClickListener { listener.accept(training) }
		}
	}
}