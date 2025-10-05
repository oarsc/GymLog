package org.oar.gymlog.ui.main.history

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import org.oar.gymlog.databinding.FragmentHistoryBinding
import org.oar.gymlog.model.Muscle
import org.oar.gymlog.model.Training
import org.oar.gymlog.room.entities.BitEntity
import org.oar.gymlog.room.entities.TrainingEntity
import org.oar.gymlog.ui.common.ResultLauncherFragment
import org.oar.gymlog.ui.common.components.HistoryCalendarView.PieDataInfo
import org.oar.gymlog.ui.training.TrainingActivity
import org.oar.gymlog.util.Constants
import org.oar.gymlog.util.Data
import org.oar.gymlog.util.DateUtils.timeInMillis
import org.oar.gymlog.util.extensions.ComponentsExts.runOnUiThread
import org.oar.gymlog.util.extensions.DatabaseExts.dbThread
import java.time.LocalDate
import kotlin.math.ceil

class HistoryFragment : ResultLauncherFragment() {

	private lateinit var binding: FragmentHistoryBinding

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View = FragmentHistoryBinding.inflate(inflater, container, false)
        .apply {
            val context = requireContext()
            binding = this

            // LEGEND LIST
            legend.apply {
                layoutManager = object : GridLayoutManager(context, 2) {
                    override fun canScrollVertically() = false
                }
                init(Data.muscles.sortToColumns, HistoryLegendListHandler(context))
            }

            showLegend.setOnClickListener {
                if (legend.isVisible) {
                    legend.visibility = View.GONE
                    showLegendIcon.animate().rotation(0f).start()
                } else {
                    legend.visibility = View.VISIBLE
                    showLegendIcon.animate().rotation(180f).start()
                }
            }

            // TRAININGS LIST
            val listHandler = HistoryTrainingListHandler(context)
            trainingList.apply {
                unScrollableVertically = true
                init(listOf<TrainingData>(), listHandler)
            }

            listHandler.setOnClickListener { training ->
                val intent = Intent(context, TrainingActivity::class.java)
                intent.putExtra("trainingId", training.id)
                startActivityForResult(intent, Constants.IntentReference.TRAINING)
            }

            // CALENDAR VIEW
            calendarView.apply {
                setOnSelectDayListener { startDate, muscles ->
                    onDaySelected(startDate, muscles)
                }
                setOnMonthChangeListener { first, end ->
                    updateMonthData(first, end)
                }
            }

            // TOOLBAR
            toolbar.setNavigationOnClickListener {
				requireActivity().onBackPressedDispatcher.onBackPressed()
			}
        }
        .root

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

			val initialSize = binding.trainingList.size

			val newTrainings = trainings.mapNotNull {
				val bits = db.bitDao().getHistoryByTrainingId(it.trainingId)
				if (bits.isNotEmpty()) {
					getTrainingData(it, bits)
				} else {
					null
				}
			}

			runOnUiThread {
				val legend = muscles ?: Data.muscles
				val initSize = binding.legend.size

				binding.legend.setListData(legend.sortToColumns)
				binding.legend.dynamicallyItemsChangedBySize(initSize)

				binding.trainingList.setListData(newTrainings)
				binding.trainingList.dynamicallyItemsChangedBySize(initialSize)
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
					runOnUiThread { binding.calendarView.setDayData(millis, data) }
				}

				if (binding.calendarView.isSelected(currentDay) && data.isNotEmpty()) {
					runOnUiThread {
						binding.legend.setListData(data.map(PieDataInfo::muscle).sortToColumns)
					}
				}

				currentDay = currentDay.plusDays(1)
			}
			runOnUiThread { binding.calendarView.isEnabled = true }
		}
	}

	override fun onActivityResult(intentReference: Constants.IntentReference, data: Intent) {
		when {
			intentReference === Constants.IntentReference.TRAINING -> {
				if (data.getBooleanExtra("refresh", false)) {
					val day = binding.calendarView.selectedDay
					val muscles = binding.calendarView.getDayMuscles(day)
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