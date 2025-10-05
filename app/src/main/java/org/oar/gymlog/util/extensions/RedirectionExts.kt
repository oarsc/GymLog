package org.oar.gymlog.util.extensions

import android.content.Context
import android.content.Intent
import org.oar.gymlog.model.Muscle
import org.oar.gymlog.model.Variation
import org.oar.gymlog.ui.common.ResultLauncherAppCompatActivity
import org.oar.gymlog.ui.common.ResultLauncherFragment
import org.oar.gymlog.ui.exercises.ExercisesActivity
import org.oar.gymlog.ui.main.MainActivity
import org.oar.gymlog.ui.registry.RegistryActivity
import org.oar.gymlog.util.Constants


object RedirectionExts {
    fun Context.goToVariation(variation: Variation) {
        if (this is ResultLauncherAppCompatActivity) goToVariation(variation)
    }

    fun ResultLauncherAppCompatActivity.goToVariation(variation: Variation, muscle: Muscle? = null) {
        internalGoToVariation(variation, this, this::startActivity, this::startActivityForResult, muscle)
    }

    fun ResultLauncherFragment.goToVariation(variation: Variation, muscle: Muscle? = null) {
        internalGoToVariation(variation, context, this::startActivity, this::startActivityForResult, muscle)
    }

    private fun internalGoToVariation(
        variation: Variation,
        context: Context?,
        startActivity: (Intent) -> Unit,
        startActivityForResult: (Intent, Constants.IntentReference) -> Unit,
        muscle: Muscle?
    ) {
        val exercise = variation.exercise

        Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(this)
        }

        val muscleId = muscle?.id ?: exercise.primaryMuscles[0].id
        Intent(context, ExercisesActivity::class.java).apply {
            putExtra("muscleId", muscleId)
            putExtra("expandExerciseId", exercise.id)
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