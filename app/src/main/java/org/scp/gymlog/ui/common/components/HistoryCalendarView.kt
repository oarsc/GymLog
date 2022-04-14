package org.scp.gymlog.ui.common.components

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import org.scp.gymlog.R
import org.scp.gymlog.model.Muscle
import org.scp.gymlog.util.DateUtils.firstTimeOfDay
import java.util.*
import java.util.function.BiConsumer

class HistoryCalendarView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    private val firstDayOfMonth: Calendar
    private var selectedDay: Calendar
    private val daysMap: MutableMap<Long, View> = HashMap()
    private val dayDataMap: MutableMap<Long, List<Muscle>> = HashMap()
    private var onMonthChangeListener: BiConsumer<Calendar, Calendar>? = null
    private var onSelectDayListener: BiConsumer<Calendar, List<Muscle>?>? = null

    init {
        inflate(getContext(), R.layout.view_calendar, this)

        selectedDay = Calendar.getInstance().firstTimeOfDay()
        firstDayOfMonth = selectedDay.clone() as Calendar
        firstDayOfMonth[Calendar.DAY_OF_MONTH] = 1

        findViewById<View>(R.id.prevButton).setOnClickListener { moveMonth(false) }
        findViewById<View>(R.id.nextButton).setOnClickListener { moveMonth(true) }

        updateHeader()
        drawWeeks()
    }

    private fun moveMonth(next: Boolean) {
        isEnabled = false
        dayDataMap.clear()
        firstDayOfMonth.add(Calendar.MONTH, if (next) 1 else -1)
        updateHeader()
        drawWeeks()
    }

    fun isSelected(calendar: Calendar): Boolean {
        return selectedDay.compareTo(calendar) == 0
    }

    fun isSelected(value: Long): Boolean {
        return selectedDay.timeInMillis == value
    }

    private fun updateHeader() {
        val year:TextView = findViewById(R.id.year)
        year.text = firstDayOfMonth[Calendar.YEAR].toString()
        val monthRef = when (firstDayOfMonth[Calendar.MONTH]) {
            1 -> R.string.month_february
            2 -> R.string.month_march
            3 -> R.string.month_april
            4 -> R.string.month_may
            5 -> R.string.month_june
            6 -> R.string.month_july
            7 -> R.string.month_august
            8 -> R.string.month_september
            9 -> R.string.month_october
            10 -> R.string.month_november
            11 -> R.string.month_december
            else -> R.string.month_january
        }
        val month: TextView = findViewById(R.id.monthName)
        month.setText(monthRef)
    }

    private fun drawWeeks() {
        val weeks: ViewGroup = findViewById(R.id.weeks)

        weeks.removeAllViewsInLayout()
        daysMap.clear()

        val month = firstDayOfMonth[Calendar.MONTH]
        val firstDay = calculateFirstDay()
        val lastDay = firstDay.clone() as Calendar
        var j = 0
        do {
            inflate(context, R.layout.view_calendar_week, weeks)
            val week: ViewGroup = weeks.getChildAt(j++).findViewById(R.id.week)

            for (i in 0..6) {
                inflate(context, R.layout.view_calendar_day, week)
                val day = week.getChildAt(i)
                if (lastDay[Calendar.MONTH] != month) {
                    day.alpha = 0.35f
                }

                val actualDay = lastDay.clone() as Calendar
                day.setOnClickListener { selectDay(actualDay.clone() as Calendar) }

                daysMap[lastDay.timeInMillis] = day
                val number: TextView = day.findViewById(R.id.dayNumber)
                number.text = lastDay[Calendar.DAY_OF_MONTH].toString()

                val chart: PieChart = day.findViewById(R.id.chart1)
                chart.isDrawHoleEnabled = false
                chart.description.text = ""
                chart.legend.isEnabled = false
                chart.isRotationEnabled = false
                chart.setTouchEnabled(false)
                lastDay.add(Calendar.DAY_OF_YEAR, 1)
            }
        } while (lastDay[Calendar.MONTH] == month)

        updateSelectedDay()

        onMonthChangeListener?.accept(firstDay, lastDay)
    }

    private fun calculateFirstDay(): Calendar {
        val firstDay = firstDayOfMonth.clone() as Calendar

        firstDay[Calendar.DAY_OF_MONTH] = 1
        if (firstDay[Calendar.DAY_OF_WEEK] != Calendar.MONDAY) {
            firstDay.add(Calendar.DAY_OF_YEAR, -1)
            firstDay[Calendar.DAY_OF_WEEK] = Calendar.MONDAY
        }

        return firstDay
    }

    private fun updateSelectedDay() {
        val day = daysMap[selectedDay.timeInMillis]
        if (day != null) {
            val number: TextView = day.findViewById(R.id.dayNumber)
            day.setBackgroundResource(R.color.backgroundAccent)
            number.paintFlags = number.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        }
    }

    private fun selectDay(selectDay: Calendar) {
        val day = daysMap[selectedDay.timeInMillis]
        if (day != null) {
            val number: TextView = day.findViewById(R.id.dayNumber)
            day.setBackgroundColor(Color.TRANSPARENT)
            number.paintFlags = number.paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv()
        }
        selectedDay = selectDay
        onSelectDayListener?.accept(selectedDay, dayDataMap[selectDay.timeInMillis])
        val currentMonth = firstDayOfMonth[Calendar.MONTH]
        val selectedMonth = selectDay[Calendar.MONTH]
        if (currentMonth != selectedMonth) {
            if (currentMonth == 11 && selectedMonth == 0) {
                moveMonth(true)
            } else if (currentMonth == 0 && selectedMonth == 11) {
                moveMonth(false)
            } else {
                moveMonth(selectedMonth > currentMonth)
            }
        }
        updateSelectedDay()
    }

    fun setDayData(dayTime: Long, values: List<PieDataInfo>?) {
        val day = daysMap[dayTime]
            ?: throw RuntimeException("COULD NOT FIND DAY $dayTime IN MONTH: " +
                    daysMap.keys.map { day -> day.toString() })

        val chart: PieChart = day.findViewById(R.id.chart1)
        if (values == null) {
            chart.visibility = INVISIBLE
            return
        }
        if (values.isEmpty()) {
            return
        }

        dayDataMap[dayTime] = values.map { pieData -> pieData.muscle }
        chart.visibility = VISIBLE

        val pieData = PieData()

        val dataSet = PieDataSet(values
                .map { pieDataInfo -> pieDataInfo.value }
                .map { value -> PieEntry(value) }, null)

        dataSet.colors = values
            .map { pieDataInfo -> pieDataInfo.muscle.color }
            .map { color -> ResourcesCompat.getColor(context.resources, color, null ) }

        dataSet.setDrawValues(false)
        dataSet.setDrawIcons(false)
        dataSet.selectionShift = 0f

        val text: TextView = day.findViewById(R.id.dayNumber)
        text.setTextColor(
            ResourcesCompat.getColor(resources, R.color.dark, context.theme)
        )

        pieData.dataSet = dataSet
        chart.data = pieData
    }

    fun setOnMonthChangeListener(onMonthChangeListener: BiConsumer<Calendar, Calendar>) {
        this.onMonthChangeListener = onMonthChangeListener

        // send current calendar
        val month = firstDayOfMonth[Calendar.MONTH]
        val firstDay = calculateFirstDay()
        val lastDay = firstDay.clone() as Calendar
        do {
            for (i in 0..6) {
                lastDay.add(Calendar.DAY_OF_YEAR, 1)
            }
        } while (lastDay[Calendar.MONTH] == month)
        onMonthChangeListener.accept(firstDay, lastDay)
    }

    fun setOnSelectDayListener(onSelectDayListener: BiConsumer<Calendar, List<Muscle>?>) {
        this.onSelectDayListener = onSelectDayListener

        // send current selected day
        onSelectDayListener.accept(selectedDay, dayDataMap[selectedDay.timeInMillis])
    }

    override fun setEnabled(enable: Boolean) {
        findViewById<View>(R.id.prevButton).isEnabled = enable
        findViewById<View>(R.id.nextButton).isEnabled = enable
    }

    class PieDataInfo(val value: Float, val muscle: Muscle)
}