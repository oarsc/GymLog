package org.scp.gymlog.ui.common.components;

import static org.scp.gymlog.util.DateUtils.getFirstTimeOfDay;
import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.DAY_OF_WEEK;
import static java.util.Calendar.DAY_OF_YEAR;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import org.scp.gymlog.R;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;

public class HistoryCalendarView extends FrameLayout {

    private final Calendar firstDayOfMonth;
    private Calendar selectedDay;

    private final Map<Long, View> daysMap;
    private BiConsumer<Calendar, Calendar> onChangeListener;
    private Consumer<Calendar> onSelectDayListener;

    public HistoryCalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(getContext(), R.layout.view_calendar, this);

        daysMap = new HashMap<>();
        selectedDay = getFirstTimeOfDay(Calendar.getInstance());

        firstDayOfMonth = (Calendar) selectedDay.clone();
        firstDayOfMonth.set(DAY_OF_MONTH, 1);

        findViewById(R.id.prevButton).setOnClickListener(v -> moveMonth(false));
        findViewById(R.id.nextButton).setOnClickListener(v -> moveMonth(true));

        updateHeader();
        drawWeeks();
    }

    private void moveMonth(boolean next) {
        setEnabled(false);
        firstDayOfMonth.add(MONTH, next? 1 : -1);
        updateHeader();
        drawWeeks();
    }

    private void updateHeader() {
        TextView year = findViewById(R.id.year);
        year.setText(String.valueOf(firstDayOfMonth.get(YEAR)));

        int monthRef;
        switch (firstDayOfMonth.get(MONTH)) {
            case 1:  monthRef = R.string.month_february;  break;
            case 2:  monthRef = R.string.month_march;     break;
            case 3:  monthRef = R.string.month_april;     break;
            case 4:  monthRef = R.string.month_may;       break;
            case 5:  monthRef = R.string.month_june;      break;
            case 6:  monthRef = R.string.month_july;      break;
            case 7:  monthRef = R.string.month_august;    break;
            case 8:  monthRef = R.string.month_september; break;
            case 9:  monthRef = R.string.month_october;   break;
            case 10: monthRef = R.string.month_november;  break;
            case 11: monthRef = R.string.month_december;  break;
            default: monthRef = R.string.month_january;   break;
        }
        TextView month = findViewById(R.id.monthName);
        month.setText(monthRef);
    }

    private void drawWeeks() {
        ViewGroup weeks = findViewById(R.id.weeks);

        weeks.removeAllViewsInLayout();
        daysMap.clear();

        int month = firstDayOfMonth.get(MONTH);
        Calendar firstDay = calculateFirstDay();
        Calendar lastDay = (Calendar) firstDay.clone();
        int j = 0;
        do {
            inflate(getContext(), R.layout.view_calendar_week, weeks);
            ViewGroup week = weeks.getChildAt(j++).findViewById(R.id.week);

            for (int i = 0; i < 7; i++) {
                inflate(getContext(), R.layout.view_calendar_day, week);
                View day = week.getChildAt(i);
                if (lastDay.get(MONTH) != month) {
                    day.setAlpha(0.35f);
                }

                Calendar actualDay = (Calendar) lastDay.clone();
                day.setOnClickListener(v -> selectDay((Calendar) actualDay.clone()));

                daysMap.put(lastDay.getTimeInMillis(), day);
                TextView number = day.findViewById(R.id.dayNumber);
                number.setText(String.valueOf(lastDay.get(DAY_OF_MONTH)));

                PieChart chart = day.findViewById(R.id.chart1);
                chart.setDrawHoleEnabled(false);
                chart.getDescription().setText("");
                chart.getLegend().setEnabled(false);
                chart.setRotationEnabled(false);
                chart.setTouchEnabled(false);

                lastDay.add(DAY_OF_YEAR, 1);
            }
        } while (lastDay.get(MONTH) == month);

        updateSelectedDay();

        if (onChangeListener != null) {
            onChangeListener.accept(firstDay, lastDay);
        }
    }

    private Calendar calculateFirstDay() {
        Calendar firstDay = (Calendar) firstDayOfMonth.clone();

        firstDay.set(DAY_OF_MONTH, 1);
        if (firstDay.get(DAY_OF_WEEK) != Calendar.MONDAY) {
            firstDay.add(DAY_OF_YEAR, -1);
            firstDay.set(DAY_OF_WEEK, Calendar.MONDAY);
        }

        return firstDay;
    }

    private void updateSelectedDay() {
        View day = daysMap.get(selectedDay.getTimeInMillis());
        if (day != null) {
            TextView number = day.findViewById(R.id.dayNumber);
            day.setBackgroundResource(R.color.backgroundAccent);
            number.setPaintFlags(number.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        }
    }

    private void selectDay(Calendar selectDay) {
        View day = daysMap.get(selectedDay.getTimeInMillis());
        if (day != null) {
            TextView number = day.findViewById(R.id.dayNumber);
            day.setBackgroundColor(Color.TRANSPARENT);
            number.setPaintFlags(number.getPaintFlags() & ~Paint.UNDERLINE_TEXT_FLAG);
        }
        selectedDay = selectDay;
        if (onSelectDayListener != null) {
            onSelectDayListener.accept(selectedDay);
        }
        int currentMonth = firstDayOfMonth.get(MONTH);
        int selectedMonth = selectDay.get(MONTH);
        if (currentMonth != selectedMonth) {
            if (currentMonth == 11 && selectedMonth == 0) {
                moveMonth(true);
            } else if (currentMonth == 0 && selectedMonth == 11) {
                moveMonth(false);
            } else {
                moveMonth(selectedMonth > currentMonth);
            }
        }

        updateSelectedDay();
    }

    public void setDayData(Long dayTime, List<PieDataInfo> values) {
        View day = daysMap.get(dayTime);
        if (day == null) {
            throw new RuntimeException("COULD NOT FIND DAY "+dayTime+" IN MONTH: "+
                    daysMap.keySet().stream()
                            .map(String::valueOf)
                            .collect(Collectors.joining(",")));
        }

        PieChart chart = day.findViewById(R.id.chart1);
        if (values == null) {
            chart.setVisibility(INVISIBLE);
            return;
        }
        if (values.isEmpty()) {
            return;
        }
        chart.setVisibility(VISIBLE);

        PieData pieData = new PieData();

        PieDataSet dataSet = new PieDataSet(
                values.stream()
                        .map(PieDataInfo::getValue)
                        .map(PieEntry::new)
                        .collect(Collectors.toList()), null);

        dataSet.setColors(
                values.stream()
                        .map(PieDataInfo::getColor)
                        .collect(Collectors.toList()));

        dataSet.setDrawValues(false);
        dataSet.setDrawIcons(false);
        dataSet.setSelectionShift(0f);

        TextView text = day.findViewById(R.id.dayNumber);
        text.setTextColor(
                ResourcesCompat.getColor(getResources(), R.color.dark, getContext().getTheme())
        );

        pieData.setDataSet(dataSet);
        chart.setData(pieData);
    }

    public void setOnChangeListener(BiConsumer<Calendar, Calendar> onChangeListener) {
        this.onChangeListener = onChangeListener;

        // send current calendar
        if (onChangeListener != null) {
            int month = firstDayOfMonth.get(MONTH);
            Calendar firstDay = calculateFirstDay();
            Calendar lastDay = (Calendar) firstDay.clone();
            do {
                for (int i = 0; i < 7; i++) {
                    lastDay.add(DAY_OF_YEAR, 1);
                }
            } while (lastDay.get(MONTH) == month);

            onChangeListener.accept(firstDay, lastDay);
        }
    }

    public void setOnSelectDayListener(Consumer<Calendar> onSelectDayListener) {
        this.onSelectDayListener = onSelectDayListener;

        // send current selected day
        if (onSelectDayListener != null) {
            onSelectDayListener.accept(selectedDay);
        }
    }

    public void setEnabled(boolean enable) {
        findViewById(R.id.prevButton).setEnabled(enable);
        findViewById(R.id.nextButton).setEnabled(enable);
    }

    @Getter @Setter
    public static class  PieDataInfo {
        private float value;
        private int color;
    }
}
