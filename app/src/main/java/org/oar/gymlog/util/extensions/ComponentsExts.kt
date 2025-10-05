package org.oar.gymlog.util.extensions

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.view.View
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import org.oar.gymlog.R
import org.oar.gymlog.ui.common.BindingAppCompatActivity
import org.oar.gymlog.ui.common.animations.ResizeHeightAnimation
import org.oar.gymlog.ui.common.animations.ResizeWidthAnimation
import org.oar.gymlog.util.Constants.IntentReference

object ComponentsExts {
    fun Fragment.runOnUiThread(action: Runnable) = requireActivity().runOnUiThread(action)
    fun View.runOnUiThread(action: Runnable) = (context as Activity).runOnUiThread(action)

    fun Activity.startActivityWithSideTransaction(intent: Intent, toLeft: Boolean = true) {
        val options = if (toLeft) {
            ActivityOptions.makeCustomAnimation(this, R.anim.slide_in_right, R.anim.slide_out_left)
        } else {
            ActivityOptions.makeCustomAnimation(this, R.anim.slide_in_left_back, R.anim.slide_out_right_back)
        }

        startActivity(intent, options.toBundle())
    }

    fun BindingAppCompatActivity<*>.startActivityForResultWithSideTransaction(intent: Intent, intentReference: IntentReference, toLeft: Boolean = true) {
        val options = if (toLeft) {
            ActivityOptionsCompat.makeCustomAnimation(this, R.anim.slide_in_right, R.anim.slide_out_left)
        } else {
            ActivityOptionsCompat.makeCustomAnimation(this, R.anim.slide_in_left_back, R.anim.slide_out_right_back)
        }

        startActivityForResult(intent, intentReference, options)
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