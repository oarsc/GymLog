package org.scp.gymlog.util.extensions

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.Fragment
import org.scp.gymlog.exceptions.LoadException
import java.io.IOException

object ComponentsExts {
    fun Fragment.runOnUiThread(action: Runnable) = requireActivity().runOnUiThread(action)
    fun View.runOnUiThread(action: Runnable) = (context as Activity).runOnUiThread(action)

    fun ImageView.assetImage(context: Context, assetName: String) {
        val fileName = "previews/$assetName.png"
        try {
            val ims = context.assets.open(fileName)
            val d = Drawable.createFromStream(ims, null)
            setImageDrawable(d)

        } catch (e: IOException) {
            throw LoadException("Could not read \"$assetName.png\"", e)
        }
    }
}