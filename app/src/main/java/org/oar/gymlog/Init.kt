package org.oar.gymlog

import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.AlarmManager
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Handler
import android.os.HandlerThread
import android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.core.net.toUri
import androidx.lifecycle.LifecycleCoroutineScope
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
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
import org.oar.gymlog.ui.LoadActivity
import org.oar.gymlog.ui.common.dialogs.TextDialogFragment
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
import kotlin.coroutines.resume

object Init {
    fun AppCompatActivity.startUp(lifecycleScope: LifecycleCoroutineScope, onEnd: suspend (firstLoad: Boolean) -> Unit) {
        AppCompatDelegate.setDefaultNightMode(if (loadBoolean(THEME)) MODE_NIGHT_YES else MODE_NIGHT_NO)

        val notificationPermissionHandler = NotificationPermissionHandler(this)

        lifecycleScope.launch {
            // Load permissions
            if (checkSelfPermission(POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                afterNotificationPermission()
            } else {
                requestNotificationPermissionSuspend(notificationPermissionHandler)
                NotificationService(this@startUp).createNotificationsChannel()
                afterNotificationPermission()
            }

            // Load data
            if (Data.exercises.isEmpty()) {
                WeightUtils.setConvertParameters(
                    loadBoolean(UNIT_CONVERSION_EXACT_VALUE),
                    loadString(UNIT_CONVERSION_STEP))

                val muscles = dbThreadSuspend { muscleDao().getOnlyMuscles() }

                if (muscles.isEmpty()) {
                    onEnd(true)
                    return@launch
                } else {
                    dbThreadSuspend {
                        loadData(this)
                    }
                }
            }
            onEnd(false)
        }
    }

    suspend fun AppCompatActivity.firstLoad(filePickerHandler: FilePickerHandler, onEnd: () -> Unit) {
        val import = showConfirmDialogSuspend()

        if (import) {
            val uri = openDocumentSuspend("application/json", filePickerHandler)

            if (uri == null) {
                dbThreadSuspend {
                    InitialDataService.persist(assets, this)
                    loadData(this)
                }
            } else {
                val intent = Intent(this@firstLoad, LoadActivity::class.java)
                intent.putExtra("import", uri)
                startActivity(intent)
                finish()
                return
            }

        } else {
            dbThreadSuspend {
                InitialDataService.persist(assets, this)
                loadData(this)
            }
        }
        onEnd()
    }

    private suspend fun requestNotificationPermissionSuspend(notificationPermissionHandler: NotificationPermissionHandler): Boolean = withContext(Dispatchers.Main) {
        suspendCancellableCoroutine { cont ->
            notificationPermissionHandler.continuation = cont
            notificationPermissionHandler.notificationPermissionLauncher.launch(POST_NOTIFICATIONS)
        }
    }

    private suspend fun AppCompatActivity.showConfirmDialogSuspend(): Boolean = withContext(Dispatchers.Main) {
        suspendCancellableCoroutine { cont ->

            val dialog = TextDialogFragment(
                title = R.string.dialog_confirm_import_title,
                textId = R.string.dialog_confirm_import_text,
                callback = { if (cont.isActive) cont.resume(it) }
            )

            cont.invokeOnCancellation {
                if (dialog.isAdded) {
                    dialog.dismissAllowingStateLoss()
                }
            }

            dialog.show(supportFragmentManager, null)
        }
    }

    private suspend fun openDocumentSuspend(mimeType: String, filePickerHandler: FilePickerHandler): Uri? =
        suspendCancellableCoroutine { cont ->
            filePickerHandler.continuation = cont
            filePickerHandler.filePickerLauncher.launch(arrayOf(mimeType))

            cont.invokeOnCancellation {
                filePickerHandler.continuation = null
            }
        }

    private suspend fun Context.afterNotificationPermission() =
        suspendCancellableCoroutine { cont ->
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
                            if (cont.isActive) cont.resume(Unit)
                            quitSafely()
                        }
                    }
                }
            } else {
                cont.resume(Unit)
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

    class FilePickerHandler(activity: AppCompatActivity) {
        var continuation: CancellableContinuation<Uri?>? = null
        val filePickerLauncher: ActivityResultLauncher<Array<String>> =
            activity.registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
                continuation?.let { cont ->
                    if (cont.isActive) cont.resume(uri)
                    continuation = null
                }
            }
    }

    class NotificationPermissionHandler(activity: AppCompatActivity) {
        var continuation: CancellableContinuation<Boolean>? = null
        val notificationPermissionLauncher: ActivityResultLauncher<String?> =
            activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                continuation?.let { cont ->
                    if (cont.isActive) cont.resume(isGranted)
                    continuation = null
                }
            }
    }
}