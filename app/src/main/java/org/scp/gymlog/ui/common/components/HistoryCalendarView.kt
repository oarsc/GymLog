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
import androidx.fragment.app.FragmentActivity
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import org.scp.gymlog.R
import org.scp.gymlog.model.Muscle
import org.scp.gymlog.ui.common.dialogs.SelectMonthDialogFragment
import org.scp.gymlog.util.DateUtils.prevMonday
import org.scp.gymlog.util.DateUtils.timeInMillis
import java.time.LocalDate
import java.time.Month
import java.util.function.BiConsumer

class HistoryCalendarView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    companion object {
        val MONTH_NAMES = mapOf(
            1 to R.string.month_january,
            2 to R.string.month_february,
            3 to R.string.month_march,
            4 to R.string.month_april,
            5 to R.string.month_may,
            6 to R.string.month_june,
            7 to R.string.month_july,
            8 to R.string.month_august,
            9 to R.string.month_september,
            10 to R.string.month_october,
            11 to R.string.month_november,
            12 to R.string.month_december,
        )
    }

    private var firstDayOfMonth: LocalDate
    var selectedDay: LocalDate
        private set
    private val daysMap: MutableMap<Long, View> = HashMap()
    private val dayDataMap: MutableMap<Long, List<Muscle>> = HashMap()
    private var onMonthChangeListener: BiConsumer<LocalDate, LocalDate>? = null
    private var onSelectDayListener: BiConsumer<LocalDate, List<Muscle>?>? = null

    init {
        inflate(getContext(), R.layout.view_calendar, this)

        selectedDay = LocalDate.now()
        firstDayOfMonth = selectedDay.withDayOfMonth(1)

        findViewById<View>(R.id.prevButton).setOnClickListener { moveMonth(false) }
        findViewById<View>(R.id.nextButton).setOnClickListener { moveMonth(true) }

        findViewById<View>(R.id.monthView).setOnClickListener {
            val dialog = SelectMonthDialogFragment(
                R.string.dialog_title_select_month,
                firstDayOfMonth.monthValue,
                firstDayOfMonth.year,
                { month, year ->
                    isEnabled = false
                    dayDataMap.clear()
                    firstDayOfMonth = LocalDate.of(year, month, 1)
                    updateHeader()
                    drawWeeks()
                }
            )

            context as FragmentActivity
            dialog.show(context.supportFragmentManager, null)
        }

        updateHeader()
        drawWeeks()
    }

    private fun moveMonth(next: Boolean) {
        isEnabled = false
        dayDataMap.clear()
        firstDayOfMonth = firstDayOfMonth.plusMonths(if (next) 1 else -1)
        updateHeader()
        drawWeeks()
    }

    fun isSelected(date: LocalDate): Boolean {
        return selectedDay.compareTo(date) == 0
    }

    fun isSelected(value: Long): Boolean {
        return selectedDay.timeInMillis == value
    }

    private fun updateHeader() {
        val year = findViewById<TextView>(R.id.year)
        year.text = firstDayOfMonth.year.toString()

        MONTH_NAMES[firstDayOfMonth.monthValue]?.also {
            val month = findViewById<TextView>(R.id.monthName)
            month.setText(it)
        }
    }

    private fun drawWeeks() {
        val weeks = findViewById<ViewGroup>(R.id.weeks)

        weeks.removeAllViewsInLayout()
        daysMap.clear()

        val month = firstDayOfMonth.month
        val firstDay = calculateFirstDay()
        var lastDay = firstDay
        var j = 0
        do {
            inflate(context, R.layout.view_calendar_week, weeks)
            val week = weeks.getChildAt(j++).findViewById<ViewGroup>(R.id.week)

            for (i in 0..6) {
                inflate(context, R.layout.view_calendar_day, week)
                val day = week.getChildAt(i)
                if (lastDay.month != month) {
                    day.alpha = 0.35f
                }

                val currentDay = lastDay
                day.setOnClickListener { selectDay(currentDay) }

                daysMap[lastDay.timeInMillis] = day
                val number = day.findViewById<TextView>(R.id.dayNumber)
                number.text = lastDay.dayOfMonth.toString()

                val chart = day.findViewById<PieChart>(R.id.chart1)
                chart.isDrawHoleEnabled = false
                chart.description.text = ""
                chart.legend.isEnabled = false
                chart.isRotationEnabled = false
                chart.setTouchEnabled(false)
                lastDay = lastDay.plusDays(1)
            }
        } while (lastDay.month == month)

        updateSelectedDay()

        onMonthChangeListener?.accept(firstDay, lastDay)
    }

    private fun calculateFirstDay(): LocalDate {
        return firstDayOfMonth.withDayOfMonth(1).prevMonday()
    }

    private fun updateSelectedDay() {
        val day = daysMap[selectedDay.timeInMillis]
        if (day != null) {
            val number = day.findViewById<TextView>(R.id.dayNumber)
            day.setBackgroundResource(R.color.backgroundAccent)
            number.paintFlags = number.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        }
    }

    private fun selectDay(selectDay: LocalDate) {
        val day = daysMap[selectedDay.timeInMillis]
        if (day != null) {
            val number = day.findViewById<TextView>(R.id.dayNumber)
            day.setBackgroundColor(Color.TRANSPARENT)
            number.paintFlags = number.paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv()
        }
        selectedDay = selectDay
        onSelectDayListener?.accept(selectedDay, getDayMuscles(selectDay))
        val currentMonth = firstDayOfMonth.month
        val selectedMonth = selectDay.month
        if (currentMonth != selectedMonth) {
            if (currentMonth == Month.DECEMBER && selectedMonth == Month.JANUARY) {
                moveMonth(true)
            } else if (currentMonth == Month.JANUARY && selectedMonth == Month.DECEMBER) {
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

        val chart = day.findViewById<PieChart>(R.id.chart1)
        if (values == null) {
            chart.visibility = INVISIBLE
            return
        }
        if (values.isEmpty()) {
            return
        }

        dayDataMap[dayTime] = values.map { pieData -> pieData.muscle }
        chart.visibility = VISIBLE

        val reversedValues = values.reversed()

        val pieValues = reversedValues
            .map { pieDataInfo -> pieDataInfo.value }
            .map { value -> PieEntry(value) }

        val dataSet = PieDataSet(pieValues, null).apply {
            colors = reversedValues
                .map { pieDataInfo -> pieDataInfo.muscle.color }
                .map { color -> ResourcesCompat.getColor(context.resources, color, null ) }

            setDrawValues(false)
            setDrawIcons(false)
            selectionShift = 0f
        }

        day.findViewById<TextView>(R.id.dayNumber)
            .setTextColor(
                ResourcesCompat.getColor(resources, R.color.dark, context.theme)
            )

        val pieData = PieData(dataSet)
        chart.data = pieData
    }

    fun setOnMonthChangeListener(onMonthChangeListener: BiConsumer<LocalDate, LocalDate>) {
        this.onMonthChangeListener = onMonthChangeListener

        // send current date
        val month = firstDayOfMonth.month
        val firstDay = calculateFirstDay()
        var lastDay = firstDay
        do {
            lastDay = lastDay.plusWeeks(1)
        } while (lastDay.month == month)
        onMonthChangeListener.accept(firstDay, lastDay)
    }

    fun setOnSelectDayListener(onSelectDayListener: BiConsumer<LocalDate, List<Muscle>?>) {
        this.onSelectDayListener = onSelectDayListener

        // send current selected day
        onSelectDayListener.accept(selectedDay, getDayMuscles(selectedDay))
    }

    fun getDayMuscles(day: LocalDate) = dayDataMap[day.timeInMillis]

    override fun setEnabled(enable: Boolean) {
        findViewById<View>(R.id.prevButton).isEnabled = enable
        findViewById<View>(R.id.nextButton).isEnabled = enable
    }

    class PieDataInfo(val value: Float, val muscle: Muscle)
}