package org.scp.gymlog

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.dropbox.core.oauth.DbxCredential
import kotlinx.coroutines.launch
import org.scp.gymlog.exceptions.LoadException
import org.scp.gymlog.model.Bar
import org.scp.gymlog.model.Exercise
import org.scp.gymlog.model.Gym
import org.scp.gymlog.model.Muscle
import org.scp.gymlog.model.Training
import org.scp.gymlog.model.Variation
import org.scp.gymlog.room.AppDatabase
import org.scp.gymlog.room.entities.ExerciseEntity.WithMusclesAndVariations
import org.scp.gymlog.room.entities.MuscleEntity
import org.scp.gymlog.service.DataBaseDumperService
import org.scp.gymlog.service.InitialDataService
import org.scp.gymlog.service.NotificationService
import org.scp.gymlog.service.dropbox.DropboxApiWrapper
import org.scp.gymlog.service.dropbox.DropboxOAuthUtil
import org.scp.gymlog.service.dropbox.DropboxUploadApiResponse
import org.scp.gymlog.ui.main.MainActivity
import org.scp.gymlog.ui.preferences.PreferencesDefinition
import org.scp.gymlog.ui.preferences.PreferencesDefinition.DROPBOX_CREDENTIAL
import org.scp.gymlog.util.Constants.Dropbox.APP_KEY
import org.scp.gymlog.util.Data
import org.scp.gymlog.util.DateUtils.getTimestampString
import org.scp.gymlog.util.WeightUtils
import org.scp.gymlog.util.extensions.DatabaseExts.dbThread
import org.scp.gymlog.util.extensions.MessagingExts.toast
import org.scp.gymlog.util.extensions.PreferencesExts.loadBoolean
import org.scp.gymlog.util.extensions.PreferencesExts.loadDbxCredential
import org.scp.gymlog.util.extensions.PreferencesExts.loadInteger
import org.scp.gymlog.util.extensions.PreferencesExts.loadString
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private val dataBaseDumperService by lazy { DataBaseDumperService() }
    private val dropboxOAuthUtil by lazy { DropboxOAuthUtil(this) }
    private var onResumeActions = mutableListOf<() -> Unit>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onResumeActions.clear()

        if (loadBoolean(PreferencesDefinition.THEME) &&
                AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_YES) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            recreate()

        } else if (!importData() && !exportData() && !dropboxExportData()) {
            WeightUtils.setConvertParameters(
                loadBoolean(PreferencesDefinition.UNIT_CONVERSION_EXACT_VALUE),
                loadString(PreferencesDefinition.UNIT_CONVERSION_STEP))

            NotificationService(this).createNotificationsChannel()

            dbThread { db ->
                val muscles = db.muscleDao().getOnlyMuscles()
                if (muscles.isEmpty()) {
                    InitialDataService.persist(assets, db)
                }
                loadData(db)
                goMain()
            }
        }
    }

    private fun importData() : Boolean {
        val importUri = intent.extras?.getParcelable<Uri>("import")
        if (importUri != null) {
            intent.removeExtra("import")

            dbThread { db ->
                contentResolver.openInputStream(importUri).use { inputStream ->
                    dataBaseDumperService.load(this, inputStream!!, db)
                    runOnUiThread { recreate() }
                }
            }
            return true
        }
        return false
    }

    private fun exportData() : Boolean {
        val exportUri = intent.extras?.getParcelable<Uri>("export")
        if (exportUri != null) {
            intent.removeExtra("export")
            val fileName = getFileName(exportUri)

            dbThread { db ->
                (contentResolver.openOutputStream(exportUri) as FileOutputStream)
                    .use { fileOutputStream ->
                        dataBaseDumperService.save(this, fileOutputStream, db)
                        toast("Saved \"$fileName\"")
                        goMain()
                    }
            }
            return true
        }
        return false
    }

    private fun dropboxExportData() : Boolean {
        if (intent.action == "dropbox") {
            val dbxCredential = loadDbxCredential(DROPBOX_CREDENTIAL)

            if (dbxCredential != null) {
                uploadToDropbox(dbxCredential)
            } else {
                onResumeActions.apply {
                    add { dropboxOAuthUtil.startDropboxAuthorizationOAuth2(this@SplashActivity) }
                    add {
                        loadDbxCredential(DROPBOX_CREDENTIAL)
                            ?.also(this@SplashActivity::uploadToDropbox)
                            ?: run {
                                toast("Process cancelled")
                                goMain()
                            }
                    }
                }
            }
            return true
        }
        return false
    }

    private fun uploadToDropbox(dbxCredential: DbxCredential) {
        dbThread { db ->
            val inputStream = dataBaseDumperService.save(this, null, db)!!
            val fileName = "output-${LocalDateTime.now().getTimestampString()}.json"

            lifecycleScope.launch {
                val dropboxApiWrapper = DropboxApiWrapper(
                    dbxCredential = dbxCredential,
                    clientIdentifier = "db-$APP_KEY"
                )

                val response = dropboxApiWrapper.uploadFile(fileName, inputStream)
                when (response) {
                    is DropboxUploadApiResponse.Failure ->
                        toast("Error uploading file")
                    is DropboxUploadApiResponse.Success ->
                        toast("Uploaded \"${response.fileMetadata.name}\"")
                }
                goMain()
            }
        }
    }

    private fun loadData(db: AppDatabase) {
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
            .map { x: WithMusclesAndVariations ->
                val exercise = Exercise(x.exercise!!)

                x.primaryMuscles!!
                    .map(MuscleEntity::muscleId)
                    .map { muscleId ->
                        Data.muscles
                            .filter { group: Muscle -> group.id == muscleId }
                            .getOrElse(0) {
                                throw LoadException("Muscle $muscleId not found in local structure") }
                    }
                    .also { exercise.primaryMuscles.addAll(it) }

                x.secondaryMuscles!!
                    .map(MuscleEntity::muscleId)
                    .map { muscleId ->
                        Data.muscles
                            .filter { group: Muscle -> group.id == muscleId }
                            .getOrElse(0) {
                                throw LoadException("Muscle $muscleId not found in local structure") }
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

        val gymId = loadInteger(PreferencesDefinition.CURRENT_GYM)
        if (gymId > 0) {
            Data.gym = Data.getGym(gymId)
        }
    }

    private fun getFileName(uri: Uri): String {
        return when (uri.scheme) {
            ContentResolver.SCHEME_CONTENT ->
                runCatching {
                    contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                        cursor.moveToFirst()
                        cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME).let(cursor::getString)
                    } ?: ""
                }.getOrDefault("")
            else -> uri.path?.let(::File)?.name ?: ""
        }
    }

    private fun goMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("loaded", true)
        intent.putExtra("variationId",
            this.intent.extras?.getInt("variationId", -1) ?: -1
        )
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    override fun onResume() {
        super.onResume()
        dropboxOAuthUtil.onResume()
        onResumeActions.removeFirstOrNull()?.also { it() }
    }
}