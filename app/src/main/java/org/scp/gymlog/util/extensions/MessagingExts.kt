package org.scp.gymlog.util.extensions

import android.app.Activity
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

object MessagingExts {
    fun Fragment.toast(@StringRes resId: Int, duration: Int = Toast.LENGTH_LONG) {
        internalToast(requireActivity(), resId, duration)
    }
    fun Fragment.toast(text: CharSequence, duration: Int = Toast.LENGTH_LONG) {
        internalToast(requireActivity(), text, duration)
    }
    fun Fragment.snackbar(
        @StringRes resId: Int,
        @IdRes viewId: Int = android.R.id.content,
        duration: Int = Snackbar.LENGTH_LONG
    ) {
        internalSnackbar(requireActivity(), resId, viewId, duration)
    }
    fun Fragment.snackbar(
        text: CharSequence,
        @IdRes viewId: Int = android.R.id.content,
        duration: Int = Snackbar.LENGTH_LONG
    ) {
        internalSnackbar(requireActivity(), text, viewId, duration)
    }
    fun View.toast(@StringRes resId: Int, duration: Int = Toast.LENGTH_LONG) {
        internalToast(context as Activity, resId, duration)
    }
    fun View.toast(text: CharSequence, duration: Int = Toast.LENGTH_LONG) {
        internalToast(context as Activity, text, duration)
    }
    fun View.snackbar(
        @StringRes resId: Int,
        @IdRes viewId: Int = android.R.id.content,
        duration: Int = Snackbar.LENGTH_LONG
    ) {
        internalSnackbar(context as Activity, resId, viewId, duration)
    }
    fun View.snackbar(
        text: CharSequence,
        @IdRes viewId: Int = android.R.id.content,
        duration: Int = Snackbar.LENGTH_LONG
    ) {
        internalSnackbar(context as Activity, text, viewId, duration)
    }
    fun Activity.toast(@StringRes resId: Int, duration: Int = Toast.LENGTH_LONG) {
        internalToast(this, resId, duration)
    }
    fun Activity.toast(text: CharSequence, duration: Int = Toast.LENGTH_LONG) {
        internalToast(this, text, duration)
    }
    fun Activity.snackbar(
        @StringRes resId: Int,
        @IdRes viewId: Int = android.R.id.content,
        duration: Int = Snackbar.LENGTH_LONG
    ) {
        internalSnackbar(this, resId, viewId, duration)
    }
    fun Activity.snackbar(
        text: CharSequence,
        @IdRes viewId: Int = android.R.id.content,
        duration: Int = Snackbar.LENGTH_LONG
    ) {
        internalSnackbar(this, text, viewId, duration)
    }

    private fun internalToast(activity: Activity, @StringRes resId: Int, duration: Int) {
        if (Looper.getMainLooper().isCurrentThread) {
            Toast.makeText(activity, resId, duration).show()
        } else {
            activity.runOnUiThread { Toast.makeText(activity, resId, duration).show() }
        }
    }

    private fun internalToast(activity: Activity, text: CharSequence, duration: Int) {
        if (Looper.getMainLooper().isCurrentThread) {
            Toast.makeText(activity, text, duration).show()
        } else {
            activity.runOnUiThread { Toast.makeText(activity, text, duration).show() }
        }
    }

    private fun internalSnackbar(
        activity: Activity,
        @StringRes resId: Int,
        @IdRes viewId: Int,
        duration: Int
    ) {
        if (Looper.getMainLooper().isCurrentThread) {
            Snackbar.make(activity.findViewById(viewId), resId, duration).show()
        } else {
            activity.runOnUiThread {
                Snackbar.make(activity.findViewById(viewId), resId, duration).show()
            }
        }
    }

    private fun internalSnackbar(
        activity: Activity,
        text: CharSequence,
        @IdRes viewId: Int,
        duration: Int
    ) {
        if (Looper.getMainLooper().isCurrentThread) {
            Snackbar.make(activity.findViewById(viewId), text, duration).show()
        } else {
            activity.runOnUiThread {
                Snackbar.make(activity.findViewById(viewId), text, duration).show()
            }
        }
    }
}