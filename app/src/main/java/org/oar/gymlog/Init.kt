package org.oar.gymlog

import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.Activity
import android.app.AlarmManager
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Handler
import android.os.HandlerThread
import android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.net.toUri
import androidx.lifecycle.LifecycleCoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.oar.gymlog.exceptions.LoadException
import org.oar.gymlog.model.Bar
import org.oar.gymlog.model.Exercise
import org.oar.gymlog.model.Gym
import org.oar.gymlog.model.Muscle
import org.oar.gymlog.model.Training
import org.oar.gymlog.model.Variation
import org.oar.gymlog.room.AppDatabase
import org.oar.gymlog.room.entities.ExerciseEntity
import org.oar.gymlog.room.entities.MuscleEntity
import org.oar.gymlog.service.InitialDataService
import org.oar.gymlog.service.NotificationService
import org.oar.gymlog.ui.main.preferences.PreferencesDefinition.CURRENT_GYM
import org.oar.gymlog.ui.main.preferences.PreferencesDefinition.THEME
import org.oar.gymlog.ui.main.preferences.PreferencesDefinition.UNIT_CONVERSION_EXACT_VALUE
import org.oar.gymlog.ui.main.preferences.PreferencesDefinition.UNIT_CONVERSION_STEP
import org.oar.gymlog.util.Data
import org.oar.gymlog.util.WeightUtils
import org.oar.gymlog.util.extensions.DatabaseExts.dbThreadSuspend
import org.oar.gymlog.util.extensions.PreferencesExts.loadBoolean
import org.oar.gymlog.util.extensions.PreferencesExts.loadInteger
import org.oar.gymlog.util.extensions.PreferencesExts.loadString

object Init {
    fun Activity.startUp(lifecycleScope: LifecycleCoroutineScope, onEnd: () -> Unit) {
        if (loadBoolean(THEME) &&
            AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_YES) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }

        lifecycleScope.launch {
            // Load permissions
            if (checkSelfPermission(POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                afterNotificationPermission()
            } else {
                requestPermissions(arrayOf(POST_NOTIFICATIONS), 66)
                NotificationService(this@startUp).createNotificationsChannel()
                afterNotificationPermission()
            }

            // Load data
            if (Data.exercises.isEmpty()) {
                WeightUtils.setConvertParameters(
                    loadBoolean(UNIT_CONVERSION_EXACT_VALUE),
                    loadString(UNIT_CONVERSION_STEP))

                dbThreadSuspend { db ->
                    val muscles = db.muscleDao().getOnlyMuscles()
                    if (muscles.isEmpty()) {
                        InitialDataService.persist(assets, db)
                    }
                    loadData(db)
                }
            }

            onEnd()
        }
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun Context.afterNotificationPermission() =
        suspendCancellableCoroutine<Unit> { continuation ->
            val alarmService = getSystemService(ALARM_SERVICE) as AlarmManager
            if (!alarmService.canScheduleExactAlarms()) {
                HandlerThread("alarm_service_intent_thread").apply {
                    start()
                    Handler(looper).post {
                        try {
                            startActivity(
                                Intent(ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                                    .apply { data = "package:$packageName".toUri() }
                            )
                        } finally {
                            if (continuation.isActive) continuation.resume(Unit) {}
                            quitSafely()
                        }
                    }
                }
            } else {
                continuation.resume(Unit) {}
            }
        }

    private fun Context.loadData(db: AppDatabase) {
        Data.bars.clear()
        db.barDao().getAll()
            .map { Bar(it) }
            .also { Data.bars.addAll(it) }

        Data.gyms.clear()
        db.gymDao().getAll()
            .map { Gym(it) }
            .also { Data.gyms.addAll(it) }

        Data.exercises.clear()

        val defaultVariationName = resources.getString(R.string.text_default)

        db.exerciseDao().getAllWithMusclesAndVariations()
            .map { x: ExerciseEntity.WithMusclesAndVariations ->
                val exercise = Exercise(x.exercise!!)

                x.primaryMuscles!!
                    .map(MuscleEntity::muscleId)
                    .map { muscleId ->
                        Data.muscles
                            .filter { group: Muscle -> group.id == muscleId }
                            .getOrElse(0) {
                                throw LoadException("Muscle $muscleId not found in local structure")
                            }
                    }
                    .also { exercise.primaryMuscles.addAll(it) }

                x.secondaryMuscles!!
                    .map(MuscleEntity::muscleId)
                    .map { muscleId ->
                        Data.muscles
                            .filter { group: Muscle -> group.id == muscleId }
                            .getOrElse(0) {
                                throw LoadException("Muscle $muscleId not found in local structure")
                            }
                    }
                    .also { exercise.secondaryMuscles.addAll(it) }

                x.variations!!
                    .map { Variation(it, exercise) }
                    .onEach { if (it.default) it.name = defaultVariationName }
                    .sortedWith { a,b -> if (a.default) -1 else if (b.default) 1 else 0 }
                    .also { exercise.variations.addAll(it) }

                exercise
            }
            .also { Data.exercises.addAll(it) }

        db.trainingDao().getCurrentTraining()?.also {
            Data.training = Training(it)
        }

        val gymId = loadInteger(CURRENT_GYM)
        if (gymId > 0) {
            Data.gym = Data.getGym(gymId)
        }
    }
}