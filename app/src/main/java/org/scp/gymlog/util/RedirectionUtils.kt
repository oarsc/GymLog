package org.scp.gymlog.util

import android.content.Context
import android.content.Intent
import org.scp.gymlog.model.Variation
import org.scp.gymlog.ui.common.CustomAppCompatActivity
import org.scp.gymlog.ui.common.CustomFragment
import org.scp.gymlog.ui.exercises.ExercisesActivity
import org.scp.gymlog.ui.registry.RegistryActivity


object RedirectionUtils {
    fun CustomAppCompatActivity.goToVariation(variation: Variation) {
        internalGoToVariation(variation, this, this::startActivity, this::startActivityForResult)
    }

    fun CustomFragment.goToVariation(variation: Variation) {
        internalGoToVariation(variation, context, this::startActivity, this::startActivityForResult)
    }

    private fun internalGoToVariation(
        variation: Variation,
        context: Context?,
        startActivity: (Intent) -> Unit,
        startActivityForResult: (Intent, Constants.IntentReference) -> Unit
    ) {
        val exercise = variation.exercise
        val muscle = exercise.primaryMuscles[0]

        Intent(context, ExercisesActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("muscleId", muscle.id)
            startActivity(this)
        }

        Intent(context, RegistryActivity::class.java).apply {
            putExtra("exerciseId", exercise.id)
            putExtra("variationId", variation.id)
            startActivityForResult(this, Constants.IntentReference.REGISTRY)
        }
    }
}