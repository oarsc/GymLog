package org.oar.gymlog.ui.exercises

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oar.gymlog.R
import org.oar.gymlog.databinding.ActivityExercisesBinding
import org.oar.gymlog.databinding.ListitemVariationBinding
import org.oar.gymlog.model.Variation
import org.oar.gymlog.room.AppDatabase
import org.oar.gymlog.ui.common.DatabaseAppCompatActivity
import org.oar.gymlog.ui.common.components.listView.CommonListView
import org.oar.gymlog.ui.common.components.listView.SimpleListHandler
import org.oar.gymlog.util.Data
import org.oar.gymlog.util.extensions.RedirectionExts.goToVariation

class LatestActivity : DatabaseAppCompatActivity<ActivityExercisesBinding>(ActivityExercisesBinding::inflate) {

    private lateinit var variations: List<Variation>

    override fun onLoad(savedInstanceState: Bundle?, db: AppDatabase): Int {
        val trainingId = Data.training?.id
            ?: return R.string.validation_training_not_started

        variations = db.bitDao().getHistoryByTrainingIdDesc(trainingId)
            .map { it.variationId }
            .distinct()
            .map { Data.getVariation(it) }

        return if (variations.isEmpty())
            R.string.validation_no_exercise_registered
        else
            CONTINUE
    }

    @SuppressLint("RestrictedApi")
    override fun onDelayedCreate(savedInstanceState: Bundle?) {
        binding.toolbar.apply {
            setTitle(R.string.title_latest)
            setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
            menu.clear()
        }
        binding.fabTraining.visibility = View.GONE

        binding.exercisesList.init(variations, object : SimpleListHandler<Variation, ListitemVariationBinding> {
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