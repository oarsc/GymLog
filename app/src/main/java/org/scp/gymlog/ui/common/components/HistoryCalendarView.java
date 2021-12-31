package org.scp.gymlog.ui.common.components;

import static org.scp.gymlog.util.DateUtils.getFirstTimeOfDay;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import org.scp.gymlog.R;

import java.util.Calendar;

public class HistoryCalendarView extends FrameLayout {

    private Calendar firstDay;
    private Calendar today;
    private int focusMonth;
    private int focusYear;

    public HistoryCalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(getContext(), R.layout.view_calendar, this);

        today = getFirstTimeOfDay(Calendar.getInstance());
        aimData(today);
        drawWeeks();
    }

    private void aimData(Calendar calendar) {
        firstDay = calendar;
        focusYear = firstDay.get(Calendar.YEAR);
        focusMonth = firstDay.get(Calendar.MONTH);

        firstDay.set(Calendar.DAY_OF_MONTH, 1);
        if (firstDay.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            firstDay.add(Calendar.DAY_OF_YEAR, -1);
            firstDay.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        }
    }

    private void drawWeeks() {
        ViewGroup weeks = findViewById(R.id.weeks);

        Calendar weekDay = (Calendar) firstDay.clone();
        int j = 0;
        do {
            inflate(getContext(), R.layout.view_calendar_week, weeks);
            ViewGroup week = weeks.getChildAt(j++).findViewById(R.id.week);

            for (int i = 0; i < 7; i++) {
                inflate(getContext(), R.layout.view_calendar_day, week);

                TextView number = week.getChildAt(i).findViewById(R.id.dayNumber);


                number.setText(String.valueOf(weekDay.get(Calendar.DAY_OF_MONTH)));

                weekDay.add(Calendar.DAY_OF_YEAR, 1);
            }
        } while (weekDay.get(Calendar.MONTH) == focusMonth);

    }
}
