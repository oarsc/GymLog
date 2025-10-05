package org.oar.gymlog.ui.common.activity

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import org.oar.gymlog.R
import org.oar.gymlog.databinding.ActivityImageSelectorBinding
import org.oar.gymlog.exceptions.LoadException
import org.oar.gymlog.ui.common.BindingAppCompatActivity
import org.oar.gymlog.util.Constants.IntentReference
import org.oar.gymlog.util.TaskRunner
import java.io.IOException

class ImageSelectorActivity : BindingAppCompatActivity<ActivityImageSelectorBinding>(ActivityImageSelectorBinding::inflate) {

    private val defaultLayoutParams by lazy { RelativeLayout.LayoutParams(175, 175) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val title = intent.extras!!.getInt("title")
        if (title == IntentReference.CREATE_EXERCISE.ordinal) {
            try {
                loadExercises()
            } catch (e: IOException) {
                throw LoadException("Error loading exercises", e)
            }
        }

        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    @Throws(IOException::class)
    private fun loadExercises() {
        val folder = "previews"
        val tr = TaskRunner()
        for (asset in assets.list(folder)!!) {
            if (asset == "others") continue
            tr.executeAsync(
                { getImageViewFromAsset("$folder/$asset") }
            ) { view -> binding.imageSelectorLayout.addView(view, defaultLayoutParams) }
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