package org.scp.gymlog.ui.latest

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.scp.gymlog.R
import org.scp.gymlog.model.Variation
import org.scp.gymlog.room.AppDatabase
import org.scp.gymlog.ui.common.DBAppCompatActivity
import org.scp.gymlog.ui.common.components.TrainingFloatingActionButton
import org.scp.gymlog.util.Data
import org.scp.gymlog.util.extensions.RedirectionExts.goToVariation
import java.util.function.Consumer

class LatestExercisesActivity : DBAppCompatActivity() {

    private lateinit var variations: List<Variation>

    private lateinit var recyclerAdapter: LatestExercisesRecyclerViewAdapter
    private lateinit var exercisesRecyclerView: RecyclerView

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

        recyclerAdapter = LatestExercisesRecyclerViewAdapter(variations)

        recyclerAdapter.onClickListener = Consumer {
            goToVariation(it)
            finish()
        }

        exercisesRecyclerView = findViewById<RecyclerView>(R.id.exercisesList).apply {
            layoutManager = LinearLayoutManager(this@LatestExercisesActivity)
            adapter = recyclerAdapter
        }

        findViewById<TrainingFloatingActionButton>(R.id.fabTraining).updateFloatingActionButton()
    }
}