package org.scp.gymlog.ui.common.animations

import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation
import org.scp.gymlog.util.FormatUtils

class ResizeHeightAnimation(
    private val mView: View,
    height: Int,
    duration: Long,
    toDp: Boolean = false
) : Animation() {

    private val mStartHeight = mView.height
    private val mHeight = if (toDp)
        FormatUtils.toDp(mView.resources.displayMetrics, height) else
        height

    init {
        setDuration(duration)
    }

    override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
        val newHeight = mStartHeight + ((mHeight - mStartHeight) * interpolatedTime).toInt()

        if (newHeight == 0) {
            mView.visibility = View.GONE
        } else if (mView.visibility == View.GONE) {
            mView.visibility = View.VISIBLE
        }

        mView.layoutParams.height = newHeight
        mView.requestLayout()
    }

    override fun willChangeBounds(): Boolean {
        return true
    }
}