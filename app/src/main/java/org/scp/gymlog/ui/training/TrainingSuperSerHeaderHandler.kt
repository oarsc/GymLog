package org.scp.gymlog.ui.training

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import org.scp.gymlog.R
import org.scp.gymlog.databinding.ListitemHistoryExerciseRowBinding
import org.scp.gymlog.exceptions.LoadException
import org.scp.gymlog.model.Variation
import org.scp.gymlog.ui.common.components.listView.CommonListView
import org.scp.gymlog.ui.common.components.listView.SimpleListHandler
import org.scp.gymlog.ui.common.components.listView.SimpleListView
import java.io.IOException
import java.util.function.Consumer

class TrainingSuperSerHeaderHandler(
    private val context: Context
) : SimpleListHandler<Variation, ListitemHistoryExerciseRowBinding> {
    override val useListState = false
    override val itemInflater: (LayoutInflater, ViewGroup?, Boolean) -> ListitemHistoryExerciseRowBinding
        = ListitemHistoryExerciseRowBinding::inflate

    private var onLongImageClickListener: Consumer<Variation>? = null

    fun setOnLongImageClickListener(onLongImageClickListener: Consumer<Variation>) {
        this.onLongImageClickListener = onLongImageClickListener
    }

    @SuppressLint("SetTextI18n")
    override fun buildListView(
        binding: ListitemHistoryExerciseRowBinding,
        item: Variation,
        index: Int,
        state: CommonListView.ListElementState?
    ) {
        val exercise = item.exercise

        binding.title.text = exercise.name
        if (item.default) {
            binding.subtitle.setText(R.string.text_default)
        } else {
            binding.subtitle.text = item.name
        }

        binding.indicator.setCardBackgroundColor(
            ResourcesCompat.getColor(context.resources, exercise.primaryMuscles[0].color, null))

        binding.cardText.text = (index + 1).toString()

        val fileName = "previews/" + exercise.image + ".png"
        try {
            val ims = context.assets.open(fileName)
            val d = Drawable.createFromStream(ims, null)
            binding.image.setImageDrawable(d)

        } catch (e: IOException) {
            throw LoadException("Could not read \"$fileName\"", e)
        }

        binding.image.setOnLongClickListener {
            onLongImageClickListener?.accept(item)
            true
        }
    }
}