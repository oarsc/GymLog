package org.scp.gymlog.ui.common.animations

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation
import androidx.core.graphics.ColorUtils
import org.scp.gymlog.R


class BackgroundColorAnimation(
    context: Context,
    private val mView: View,
    private val color: Int,
    startBackground: Drawable? = mView.background,
    durationFadeIn: Long = 200,
    durationFadeOut: Long = durationFadeIn
) : Animation() {

    private val edgeTime = durationFadeIn.toFloat() / (durationFadeIn + durationFadeOut)

    private val mStartColor = startBackground.let {
        if (it is ColorDrawable) it.color
        else context.getColor(R.color.windowBackground)
    }
    private val mOriginalBackground = mView.background

    init {
        setDuration(durationFadeIn + durationFadeOut)
    }

    override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
        if (interpolatedTime >= 1f) {
            mView.background = mOriginalBackground
        } else if (interpolatedTime <= edgeTime) {
            mView.setBackgroundColor(ColorUtils.blendARGB(mStartColor, color,  interpolatedTime / edgeTime))
        } else {
            val revertedEdgeTime = 1 - edgeTime
            val fadeOutInterpolatedTime = interpolatedTime - edgeTime
            mView.setBackgroundColor(ColorUtils.blendARGB(mStartColor, color, 1 - fadeOutInterpolatedTime / revertedEdgeTime))
        }
    }
}