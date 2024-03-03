package org.scp.gymlog.util.extensions

import android.app.Activity
import android.view.View
import androidx.fragment.app.Fragment
import org.scp.gymlog.R

object ComponentsExts {
    fun Fragment.runOnUiThread(action: Runnable) = requireActivity().runOnUiThread(action)
    fun View.runOnUiThread(action: Runnable) = (context as Activity).runOnUiThread(action)

    fun Activity.overridePendingSideTransition(toLeft: Boolean = true) {
        if (toLeft) {
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        } else {
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
        }
    }
}