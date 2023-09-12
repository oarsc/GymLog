package org.scp.gymlog.ui.common.components

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.annotation.ColorRes
import org.scp.gymlog.exceptions.LoadException
import java.io.IOException

class ExerciseImageView(context: Context, attrs: AttributeSet?) : RelativeLayout(context, attrs) {

    fun setImage(asset: String, @ColorRes color: Int) {
        this.removeAllViews()

        createImageView("previews/$asset.png")
            .also { addView(it, this.layoutParams) }

        createImageView("masks/$asset.png")
            .also { addView(it, this.layoutParams) }

        createImageView("masks/$asset.png")
            .also {
                val colorInt = context.getColor(color)

                val argb = Color.argb(
                    200,
                    Color.red(colorInt),
                    Color.green(colorInt),
                    Color.blue(colorInt)
                )

                it.imageTintList = ColorStateList.valueOf(argb)
                addView(it, this.layoutParams)
            }
    }

    private fun createImageView(assetPath: String): ImageView {
        val imageView = ImageView(context)
        imageView.setImageDrawable(getDrawable(assetPath))
        return imageView
    }

    private fun getDrawable(assetPath: String): Drawable {
        try {
            val ims = context.assets.open(assetPath)
            return Drawable.createFromStream(ims, null)
        } catch (e: IOException) {
            throw LoadException("Could not read \"$assetPath\"", e)
        }
    }
}