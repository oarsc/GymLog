package org.oar.gymlog.ui.common.animations

import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation
import org.oar.gymlog.util.FormatUtils

class ResizeWidthAnimation(
    private val mView: View,
    width: Int = CALCULATE,
    duration: Long = 250,
    toDp: Boolean = false
) : Animation() {

    companion object {
        const val CALCULATE = -1
    }

    private val mStartWidth = mView.width
    private val mWidth: Int

    init {
        mWidth = if (width == CALCULATE) {
            mView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
            mView.measuredWidth
        } else if (toDp){
            FormatUtils.toDp(mView.resources.displayMetrics, width)
        } else {
            width
        }

        setDuration(duration)
    }

    override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
        val newWidth = mStartWidth + ((mWidth - mStartWidth) * interpolatedTime).toInt()
        mView.layoutParams.width = newWidth
        mView.requestLayout()
    }

    override fun willChangeBounds(): Boolean = true
}