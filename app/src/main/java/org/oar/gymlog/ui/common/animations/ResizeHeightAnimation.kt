package org.oar.gymlog.ui.common.animations

import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation
import androidx.core.view.isGone
import org.oar.gymlog.util.FormatUtils

class ResizeHeightAnimation(
    private val mView: View,
    height: Int = CALCULATE,
    duration: Long = 250,
    toDp: Boolean = false
) : Animation() {

    companion object {
        const val CALCULATE = -1
    }

    private val mStartHeight = mView.height
    private val mHeight: Int

    init {
        mHeight = if (height == CALCULATE) {
            mView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
            mView.measuredHeight
        } else if (toDp){
            FormatUtils.toDp(mView.resources.displayMetrics, height)
        } else {
            height
        }

        setDuration(duration)
    }

    override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
        val newHeight = mStartHeight + ((mHeight - mStartHeight) * interpolatedTime).toInt()

        if (newHeight == 0) {
            mView.visibility = View.GONE
        } else if (mView.isGone) {
            mView.visibility = View.VISIBLE
        }

        mView.layoutParams.height = newHeight
        mView.requestLayout()
    }

    override fun willChangeBounds(): Boolean = true
}