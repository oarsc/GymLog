package org.scp.gymlog.ui.exercises

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.scp.gymlog.R
import org.scp.gymlog.databinding.ListitemVariationBinding
import org.scp.gymlog.exceptions.LoadException
import org.scp.gymlog.model.Variation
import org.scp.gymlog.room.AppDatabase
import org.scp.gymlog.ui.common.DBAppCompatActivity
import org.scp.gymlog.ui.common.components.TrainingFloatingActionButton
import org.scp.gymlog.ui.common.components.listView.SimpleListHandler
import org.scp.gymlog.ui.common.components.listView.SimpleListView
import org.scp.gymlog.util.Data
import org.scp.gymlog.util.extensions.RedirectionExts.goToVariation
import java.io.IOException

class LatestActivity : DBAppCompatActivity() {

    private lateinit var variations: List<Variation>

    private lateinit var exercisesRecyclerView: SimpleListView<Variation, ListitemVariationBinding>

    override fun onLoad(savedInstanceState: Bundle?, db: AppDatabase): Int {
        val trainingId = Data.trainingId
            ?: return R.string.validation_training_not_started

        variations = db.bitDao().getHistoryByTrainingId(trainingId)
            .map { it.variationId }
            .reversed()
            .distinct()
            .map { Data.getVariation(it) }

        return CONTINUE
    }

    override fun onDelayedCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_exercises)

        setTitle(R.string.title_latest)

        findViewById<TrainingFloatingActionButton>(R.id.fabTraining).visibility = View.GONE

        exercisesRecyclerView = findViewById(R.id.exercisesList)
        exercisesRecyclerView.init(variations, object : SimpleListHandler<Variation, ListitemVariationBinding> {
            override val useListState = false

            override fun generateListItemInflater(): (LayoutInflater, ViewGroup?, Boolean) -> ListitemVariationBinding {
                return ListitemVariationBinding::inflate
            }

            override fun buildListView(
                binding: ListitemVariationBinding,
                item: Variation,
                index: Int,
                state: SimpleListView.ListElementState?
            ) {
                binding.time.visibility = View.GONE
                
                binding.exerciseName.text = item.exercise.name
                binding.variationName.apply {
                    if (item.default) visibility = View.GONE
                    else              text = item.name
                }

                val fileName = "previews/" + item.exercise.image + ".png"
                try {
                    val ims = assets.open(fileName)
                    val d = Drawable.createFromStream(ims, null)
                    binding.image.setImageDrawable(d)

                } catch (e: IOException) {
                    throw LoadException("Could not read \"$fileName\"", e)
                }

                binding.root.setOnClickListener {
                    goToVariation(item)
                    finish()
                }
            }

        })
    }
}