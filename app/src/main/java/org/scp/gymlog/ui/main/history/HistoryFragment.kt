package org.scp.gymlog.ui.main.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.scp.gymlog.R
import org.scp.gymlog.model.Muscle
import org.scp.gymlog.room.DBThread
import org.scp.gymlog.room.entities.BitEntity
import org.scp.gymlog.room.entities.TrainingEntity
import org.scp.gymlog.ui.common.components.HistoryCalendarView
import org.scp.gymlog.ui.common.components.HistoryCalendarView.PieDataInfo
import org.scp.gymlog.util.Data
import org.scp.gymlog.util.DateUtils.timeInMillis
import java.time.LocalDate

class HistoryFragment : Fragment() {

	private lateinit var calendarView: HistoryCalendarView
	private val historyAdapter by lazy { HistoryRecyclerViewAdapter() }
	private val legendAdapter by lazy { HistoryLegendRecyclerViewAdapter() }

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {

		val view = inflater.inflate(R.layout.fragment_history, container, false)

		// LEGEND LIST
		val legendRecyclerView: RecyclerView = view.findViewById(R.id.legend)
		legendRecyclerView.layoutManager = object : GridLayoutManager(context, 2) {
			override fun canScrollVertically(): Boolean { return false }
		}
		legendRecyclerView.adapter = legendAdapter

		val showLegendIcon: ImageView = view.findViewById(R.id.showLegendIcon)
		view.findViewById<View>(R.id.showLegend).setOnClickListener {
			if (legendRecyclerView.visibility == View.VISIBLE) {
				legendRecyclerView.visibility = View.GONE
				showLegendIcon.animate().rotation(0f).start()
			} else {
				legendRecyclerView.visibility = View.VISIBLE
				showLegendIcon.animate().rotation(180f).start()
			}
		}

		// TRAININGS LIST
		val trainingRecyclerView: RecyclerView = view.findViewById(R.id.trainingList)
		trainingRecyclerView.layoutManager = object : LinearLayoutManager(context) {
			override fun canScrollVertically(): Boolean { return false }
		}
		trainingRecyclerView.adapter = historyAdapter

		// CALENDAR VIEW
		calendarView = view.findViewById(R.id.calendarView);
		calendarView.setOnSelectDayListener { startDate, muscles ->
			onDaySelected(startDate, muscles)
		}
		calendarView.setOnMonthChangeListener { first, end ->
			updateMonthData(first, end)
		}

		// TOOLBAR
		val toolbar: Toolbar = view.findViewById(R.id.toolbar)
		toolbar.setNavigationOnClickListener { requireActivity().onBackPressed() }
		return view
	}

	private fun onDaySelected(startDate: LocalDate, muscles: List<Muscle>?) {
		val initDate = startDate.atStartOfDay()
		val endDate = initDate.plusDays(1)
		DBThread.run(requireContext()) { db ->
			val trainings = db.trainingDao().getTrainingByStartDate(initDate, endDate)

			val initialSize: Int = historyAdapter.size()
			val endSize = trainings.size
			historyAdapter.clear()

			trainings.forEach {
				val bits = db.bitDao().getHistoryByTrainingId(it.trainingId)
				if (bits.isNotEmpty()) {
					val td = getTrainingData(it, bits)
					historyAdapter.add(td)
				}
			}

			runOnUiThread {
				legendAdapter.focusMuscles(muscles)
				historyAdapter.notifyItemsChanged(initialSize, endSize)
			}
		}
	}

	private fun updateMonthData(first: LocalDate, end: LocalDate) {
		val allMuscles: List<Muscle> = Data.muscles
		DBThread.run(requireContext()) { db ->
			val bits = db.bitDao().getHistory(first.atStartOfDay(), end.atStartOfDay())

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
					.sortedWith(Comparator.comparing { (_, value) -> value[0] })
					.map { (key, value) -> PieDataInfo(value[0], key) }

				if (data.isNotEmpty()) {
					val millis = currentDay.timeInMillis
					runOnUiThread { calendarView.setDayData(millis, data) }
				}

				if (calendarView.isSelected(currentDay) && data.isNotEmpty()) {
					runOnUiThread {
						legendAdapter.focusMuscles(data.map(PieDataInfo::muscle))
					}
				}
				currentDay = currentDay.plusDays(1)
			}
			runOnUiThread { calendarView.isEnabled = true }
		}
	}

	companion object {
		fun getTrainingData(training: TrainingEntity, bits: List<BitEntity>): TrainingData {
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

			return TrainingData(
				training.trainingId,
				training.start,
				mostUsedMuscles)
		}
	}

	private fun runOnUiThread(action: Runnable) {
		requireActivity().runOnUiThread(action)
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