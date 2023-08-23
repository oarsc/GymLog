package org.scp.gymlog.util

import android.app.Activity
import android.view.View
import androidx.fragment.app.Fragment

object ComponentsUtils {
    fun Fragment.runOnUiThread(action: Runnable) = requireActivity().runOnUiThread(action)
    fun View.runOnUiThread(action: Runnable) = (context as Activity).runOnUiThread(action)
}