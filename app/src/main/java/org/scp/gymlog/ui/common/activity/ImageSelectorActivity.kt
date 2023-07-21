package org.scp.gymlog.ui.common.activity

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import com.google.android.flexbox.FlexboxLayout
import org.scp.gymlog.R
import org.scp.gymlog.exceptions.LoadException
import org.scp.gymlog.ui.common.CustomAppCompatActivity
import org.scp.gymlog.util.Constants.IntentReference
import org.scp.gymlog.util.TaskRunner
import java.io.IOException

class ImageSelectorActivity : CustomAppCompatActivity() {

    private val defaultLayoutParams by lazy { RelativeLayout.LayoutParams(175, 175) }
    private val layout: FlexboxLayout by lazy { findViewById(R.id.imageSelectorLayout) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_selector)

        val title = intent.extras!!.getInt("title")
        if (title == IntentReference.CREATE_EXERCISE.ordinal) {
            setTitle(R.string.title_create_exercise)
            try {
                loadExercises()
            } catch (e: IOException) {
                throw LoadException("Error loading exercises", e)
            }
        }
    }

    @Throws(IOException::class)
    private fun loadExercises() {
        val folder = "previews"
        val tr = TaskRunner()
        for (asset in assets.list(folder)!!) {
            tr.executeAsync(
                { getImageViewFromAsset("$folder/$asset") }
            ) { view -> layout.addView(view, defaultLayoutParams) }
        }
    }

    @Throws(IOException::class)
    private fun getImageViewFromAsset(fileName: String): View {
        val ims = assets.open(fileName)
        val drawable = Drawable.createFromStream(ims, null)

        val imageView =
            layoutInflater.inflate(R.layout.listitem_image_selector, null) as ImageView
        imageView.setImageDrawable(drawable)
        imageView.setOnClickListener {
            val returnIntent = Intent()
            returnIntent.putExtra("fileName", fileName)
            setResult(RESULT_OK, returnIntent)
            finish()
        }
        return imageView
    }
}