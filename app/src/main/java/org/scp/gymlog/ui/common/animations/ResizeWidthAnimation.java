package org.scp.gymlog.ui.common.animations;

import static org.scp.gymlog.util.FormatUtils.toDp;

import android.view.animation.Transformation;
import android.view.View;
import android.view.animation.Animation;

public class ResizeWidthAnimation extends Animation {
    private final int mWidth;
    private final int mStartWidth;
    private final View mView;

    public ResizeWidthAnimation(View view, int width, long duration) {
        mView = view;
        mWidth = toDp(view.getResources().getDisplayMetrics(), width);
        mStartWidth = view.getWidth();
        setDuration(duration);
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        int newWidth = mStartWidth + (int) ((mWidth - mStartWidth) * interpolatedTime);
        mView.getLayoutParams().width = newWidth;
        mView.requestLayout();
    }

    @Override
    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
    }

    @Override
    public boolean willChangeBounds() {
        return true;
    }
}