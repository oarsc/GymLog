package org.scp.gymlog.ui.main.history

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import org.scp.gymlog.R
import org.scp.gymlog.databinding.ListitemLegendBinding
import org.scp.gymlog.databinding.ListitemTrainingBinding
import org.scp.gymlog.model.Muscle
import org.scp.gymlog.model.Training
import org.scp.gymlog.room.entities.BitEntity
import org.scp.gymlog.room.entities.TrainingEntity
import org.scp.gymlog.ui.common.CustomFragment
import org.scp.gymlog.ui.common.components.HistoryCalendarView
import org.scp.gymlog.ui.common.components.HistoryCalendarView.PieDataInfo
import org.scp.gymlog.ui.common.components.listView.SimpleListView
import org.scp.gymlog.ui.training.TrainingActivity
import org.scp.gymlog.util.Constants
import org.scp.gymlog.util.Data
import org.scp.gymlog.util.DateUtils.timeInMillis
import org.scp.gymlog.util.extensions.ComponentsExts.runOnUiThread
import org.scp.gymlog.util.extensions.DatabaseExts.dbThread
import java.time.LocalDate
import kotlin.math.ceil

class HistoryFragment : CustomFragment() {

	private lateinit var calendarView: HistoryCalendarView

	private lateinit var legendListView: SimpleListView<Muscle, ListitemLegendBinding>
	private lateinit var trainingListView: SimpleListView<TrainingData, ListitemTrainingBinding>

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		val context = requireContext()
		val view = inflater.inflate(R.layout.fragment_history, container, false)

		// LEGEND LIST
		legendListView = view.findViewById(R.id.legend)
		legendListView.layoutManager = object : GridLayoutManager(context, 2) {
			override fun canScrollVertically() = false
		}
		legendListView.init(Data.muscles.sortToColumns, HistoryLegendListHandler(context))

		val showLegendIcon = view.findViewById<ImageView>(R.id.showLegendIcon)
		view.findViewById<View>(R.id.showLegend).setOnClickListener {
			if (legendListView.visibility == View.VISIBLE) {
				legendListView.visibility = View.GONE
				showLegendIcon.animate().rotation(0f).start()
			} else {
				legendListView.visibility = View.VISIBLE
				showLegendIcon.animate().rotation(180f).start()
			}
		}

		// TRAININGS LIST
		val listHandler = HistoryTrainingListHandler(context)
		trainingListView = view.findViewById(R.id.trainingList)
		trainingListView.unScrollableVertically = true
		trainingListView.init(listOf(), listHandler)

		listHandler.setOnClickListener { training ->
			val intent = Intent(context, TrainingActivity::class.java)
			intent.putExtra("trainingId", training.id)
			startActivityForResult(intent, Constants.IntentReference.TRAINING)
		}

		// CALENDAR VIEW
		calendarView = view.findViewById(R.id.calendarView);
		calendarView.setOnSelectDayListener { startDate, muscles ->
			onDaySelected(startDate, muscles)
		}
		calendarView.setOnMonthChangeListener { first, end ->
			updateMonthData(first, end)
		}

		// TOOLBAR
		val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
		toolbar.setNavigationOnClickListener { requireActivity().onBackPressed() }
		return view
	}

	private val List<Muscle>.sortToColumns: List<Muscle>
		get() {
			if (size < 2) return this
			val halfPoint = ceil(size / 2f).toInt()

			val firstColumn = subList(0, halfPoint)
			val secondColumn = subList(halfPoint, size)

			return (0 until halfPoint).flatMap {
				if (it < secondColumn.size)
					listOf(firstColumn[it], secondColumn[it])
				else
					listOf(firstColumn[it])
			}
		}

	private fun onDaySelected(startDate: LocalDate, muscles: List<Muscle>?) {
		val initDate = startDate.atStartOfDay()
		val endDate = initDate.plusDays(1)
		requireContext().dbThread { db ->
			val trainings = db.trainingDao().getTrainingByStartDate(initDate, endDate)

			val initialSize = trainingListView.size

			val newTrainings = trainings.mapNotNull {
				val bits = db.bitDao().getHistoryByTrainingId(it.trainingId)
				if (bits.isNotEmpty()) {
					getTrainingData(it, bits.map { it.bit })
				} else {
					null
				}
			}

			runOnUiThread {
				val legend = muscles ?: Data.muscles
				val initSize = legendListView.size

				legendListView.setListData(legend.sortToColumns)
				legendListView.dynamicallyItemsChangedBySize(initSize)

				trainingListView.setListData(newTrainings)
				trainingListView.dynamicallyItemsChangedBySize(initialSize)
			}
		}
	}

	private fun updateMonthData(first: LocalDate, end: LocalDate) {
		val allMuscles: List<Muscle> = Data.muscles
		requireContext().dbThread { db ->
			val bits = db.bitDao().getCalendarHistory(first.atStartOfDay(), end.atStartOfDay())

			var currentDay = first
			var i = 0
			while (currentDay < end) {
				val summary: Map<Muscle, FloatArray> = allMuscles
					.fold(HashMap()) { acc, muscle -> acc.apply { acc[muscle] = floatArrayOf(0f) } }

				while (i < bits.size) {
					val bit = bits[i]

					if (currentDay == bit.timestamp.toLocalDate()) {
						val exercise = Data.getVariation(bit.variationId).exercise

						val secondariesCount = exercise.secondaryMuscles.size
						val primaryShare = if (secondariesCount > 0) 7f else 9f
						val secondaryShare = if (secondariesCount > 0) 3f / secondariesCount else 0f

						exercise.primaryMuscles
							.map { muscle -> summary[muscle]!! }
							.forEach { fs -> fs[0] += primaryShare }

						exercise.secondaryMuscles
							.map { muscle -> summary[muscle]!! }
							.forEach { fs -> fs[0] += secondaryShare }
					} else break
					i++
				}
				val data = summary.entries
					.filter { (_, value) -> value[0] > 0 }
					.sortedWith(Comparator.comparing { (_, value) -> -value[0] })
					.map { (key, value) -> PieDataInfo(value[0], key) }

				if (data.isNotEmpty()) {
					val millis = currentDay.timeInMillis
					runOnUiThread { calendarView.setDayData(millis, data) }
				}

				if (calendarView.isSelected(currentDay) && data.isNotEmpty()) {
					runOnUiThread {
						legendListView.setListData(data.map(PieDataInfo::muscle).sortToColumns)
					}
				}

				currentDay = currentDay.plusDays(1)
			}
			runOnUiThread { calendarView.isEnabled = true }
		}
	}

	override fun onActivityResult(intentReference: Constants.IntentReference, data: Intent) {
		when {
			intentReference === Constants.IntentReference.TRAINING -> {
				if (data.getBooleanExtra("refresh", false)) {
					val day = calendarView.selectedDay
					val muscles = calendarView.getDayMuscles(day)
					onDaySelected(day, muscles)
				}
			}
		}
	}

	companion object {
		fun getTrainingData(entity: TrainingEntity, bits: List<BitEntity>): TrainingData {
			val musclesCount = mutableListOf<MuscleCount>()

			bits.map { Data.getVariation(it.variationId).exercise }
				.flatMap { exercise -> exercise.primaryMuscles }
				.forEach { muscle ->
					val m = musclesCount
						.filter { mc -> mc.muscle == muscle }
						.getOrNull(0)

					if (m != null) {
						m.count++
					} else {
						musclesCount.add(MuscleCount(muscle))
					}
				}

			musclesCount.sortWith { a, b -> b.count.compareTo(a.count) }

			val total = musclesCount.sumOf { muscleCount -> muscleCount.count }
			val limit = (musclesCount[0].count / total.toFloat() - 7.5f).toInt()

			val mostUsedMuscles = musclesCount
				.filter { it.count / total > limit }
				.map { it.muscle }

			return TrainingData(Training(entity), mostUsedMuscles)
		}
	}

	internal class MuscleCount(val muscle: Muscle) {
		var count = 1

		override fun equals(other: Any?): Boolean {
			return muscle == (other as MuscleCount).muscle
		}

		override fun hashCode(): Int {
			var result = muscle.hashCode()
			result = 31 * result + count
			return result
		}
	}
}