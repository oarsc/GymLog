package org.scp.gymlog.ui.common.components;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.mikephil.charting.charts.PieChart;

public class PerfectSquarePieChartView extends PieChart {

    public PerfectSquarePieChartView(@NonNull Context context) {
        super(context);
    }

    public PerfectSquarePieChartView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PerfectSquarePieChartView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getMeasuredWidth();
        setMeasuredDimension(width, width);
    }
}
