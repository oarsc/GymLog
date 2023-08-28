package org.scp.gymlog.util.extensions

import android.content.Context
import android.content.Intent
import org.scp.gymlog.model.Variation
import org.scp.gymlog.ui.common.CustomAppCompatActivity
import org.scp.gymlog.ui.common.CustomFragment
import org.scp.gymlog.ui.exercises.ExercisesActivity
import org.scp.gymlog.ui.main.MainActivity
import org.scp.gymlog.ui.registry.RegistryActivity
import org.scp.gymlog.util.Constants


object RedirectionExts {
    fun Context.goToVariation(variation: Variation) {
        (this as CustomAppCompatActivity).goToVariation(variation)
    }

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

        Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(this)
        }

        val muscle = exercise.primaryMuscles[0]
        Intent(context, ExercisesActivity::class.java).apply {
            putExtra("muscleId", muscle.id)
            addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(this)
        }

        Intent(context, RegistryActivity::class.java).apply {
            putExtra("exerciseId", exercise.id)
            putExtra("variationId", variation.id)
            startActivityForResult(this, Constants.IntentReference.REGISTRY)
        }
    }
}