package org.scp.gymlog.ui.exercises

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.scp.gymlog.R
import org.scp.gymlog.databinding.ListitemVariationBinding
import org.scp.gymlog.model.Variation
import org.scp.gymlog.room.AppDatabase
import org.scp.gymlog.ui.common.DBAppCompatActivity
import org.scp.gymlog.ui.common.components.TrainingFloatingActionButton
import org.scp.gymlog.ui.common.components.listView.CommonListView
import org.scp.gymlog.ui.common.components.listView.SimpleListHandler
import org.scp.gymlog.ui.common.components.listView.SimpleListView
import org.scp.gymlog.util.Data
import org.scp.gymlog.util.extensions.RedirectionExts.goToVariation

class LatestActivity : DBAppCompatActivity() {

    private lateinit var variations: List<Variation>

    private lateinit var exercisesRecyclerView: SimpleListView<Variation, ListitemVariationBinding>

    override fun onLoad(savedInstanceState: Bundle?, db: AppDatabase): Int {
        val trainingId = Data.training?.id
            ?: return R.string.validation_training_not_started

        variations = db.bitDao().getHistoryByTrainingIdDesc(trainingId)
            .map { it.bit.variationId }
            .distinct()
            .map { Data.getVariation(it) }

        return if (variations.isEmpty())
            R.string.validation_no_exercise_registered
        else
            CONTINUE
    }

    override fun onDelayedCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_exercises)

        setTitle(R.string.title_latest)

        findViewById<TrainingFloatingActionButton>(R.id.fabTraining).visibility = View.GONE

        exercisesRecyclerView = findViewById(R.id.exercisesList)
        exercisesRecyclerView.init(variations, object : SimpleListHandler<Variation, ListitemVariationBinding> {
            override val useListState = false
            override val itemInflater: (LayoutInflater, ViewGroup?, Boolean) -> ListitemVariationBinding
                = ListitemVariationBinding::inflate

            override fun buildListView(
                binding: ListitemVariationBinding,
                item: Variation,
                index: Int,
                state: CommonListView.ListElementState?
            ) {
                binding.exerciseName.text = item.exercise.name
                binding.variationName.apply {
                    if (item.default) visibility = View.GONE
                    else              text = item.name
                }

                binding.image.setImage(item.exercise.image, item.exercise.primaryMuscles[0].color)

                binding.root.setOnClickListener {
                    goToVariation(item)
                    finish()
                }
            }

        })
    }
}