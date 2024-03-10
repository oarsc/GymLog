package org.scp.gymlog.util.extensions

import android.app.Activity
import android.view.View
import androidx.fragment.app.Fragment
import org.scp.gymlog.R
import org.scp.gymlog.ui.common.animations.ResizeHeightAnimation
import org.scp.gymlog.ui.common.animations.ResizeWidthAnimation

object ComponentsExts {
    fun Fragment.runOnUiThread(action: Runnable) = requireActivity().runOnUiThread(action)
    fun View.runOnUiThread(action: Runnable) = (context as Activity).runOnUiThread(action)

    fun Activity.overridePendingSideTransition(toLeft: Boolean = true) {
        if (toLeft) {
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        } else {
            overridePendingTransition(R.anim.slide_in_left_back, R.anim.slide_out_right_back)
        }
    }

    fun View.startResizeWidthAnimation(
        width: Int = ResizeWidthAnimation.CALCULATE,
        duration: Long = 250,
        toDp: Boolean = false,
        animLauncher: View = this
    ) = animLauncher.startAnimation(ResizeWidthAnimation(this, width, duration, toDp))

    fun View.startResizeHeightAnimation(
        height: Int = ResizeHeightAnimation.CALCULATE,
        duration: Long = 250,
        toDp: Boolean = false,
        animLauncher: View = this
    ) = animLauncher.startAnimation(ResizeHeightAnimation(this, height, duration, toDp))
}