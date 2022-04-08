package org.scp.gymlog.ui.common.animations

import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation
import org.scp.gymlog.util.FormatUtils

class ResizeWidthAnimation(private val mView: View, width: Int, duration: Long) : Animation() {
    private val mWidth: Int = FormatUtils.toDp(mView.resources.displayMetrics, width)
    private val mStartWidth: Int = mView.width

    init {
        setDuration(duration)
    }

    override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
        val newWidth = mStartWidth + ((mWidth - mStartWidth) * interpolatedTime).toInt()
        mView.layoutParams.width = newWidth
        mView.requestLayout()
    }

    override fun willChangeBounds(): Boolean {
        return true
    }
}