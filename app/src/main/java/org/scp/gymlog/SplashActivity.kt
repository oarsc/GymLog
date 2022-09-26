package org.scp.gymlog

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import org.scp.gymlog.exceptions.LoadException
import org.scp.gymlog.model.Bar
import org.scp.gymlog.model.Exercise
import org.scp.gymlog.model.Muscle
import org.scp.gymlog.model.Variation
import org.scp.gymlog.room.AppDatabase
import org.scp.gymlog.room.DBThread
import org.scp.gymlog.room.entities.BarEntity
import org.scp.gymlog.room.entities.ExerciseEntity.WithMusclesAndVariations
import org.scp.gymlog.room.entities.MuscleEntity
import org.scp.gymlog.service.DataBaseDumperService
import org.scp.gymlog.service.InitialDataService
import org.scp.gymlog.service.NotificationService
import org.scp.gymlog.ui.main.MainActivity
import org.scp.gymlog.util.Data
import org.scp.gymlog.util.WeightUtils
import java.io.File
import java.io.FileOutputStream

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private val dataBaseDumperService by lazy { DataBaseDumperService() }
    private val initialDataService = InitialDataService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        if (preferences.getBoolean("nightTheme", false) &&
                AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_YES) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            recreate()

        } else if (!importData() && !exportData()) {
            WeightUtils.setConvertParameters(
                preferences.getBoolean("conversionExactValue", false),
                preferences.getString("conversionStep", "1")!!)

            NotificationService(this).createNotificationsChannel()

            DBThread.run(this) { db ->
                val muscles = db.muscleDao().getOnlyMuscles()
                if (muscles.isEmpty()) {
                    initialDataService.persist(assets, db)
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

            DBThread.run(this) { db ->
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

            DBThread.run(this) { db ->
                (contentResolver.openOutputStream(exportUri) as FileOutputStream)
                    .use { fileOutputStream ->
                        dataBaseDumperService.save(this, fileOutputStream, db)
                        runOnUiThread {
                            Toast.makeText(this, "Saved \"$fileName\"", Toast.LENGTH_LONG).show()
                            goMain()
                        }
                    }
            }
            return true
        }
        return false
    }

    private fun loadData(db: AppDatabase) {
        Data.bars.clear()
        db.barDao().getAll()
            .map { entity: BarEntity? -> Bar(entity!!) }
            .also { Data.bars.addAll(it) }

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
                    .map { variationEntity -> Variation(variationEntity, exercise) }
                    .onEach { variation -> if (variation.default) variation.name = defaultVariationName }
                    .sortedWith { a,b -> if (a.default) -1 else if (b.default) 1 else 0 }
                    .also { exercise.variations.addAll(it) }

                exercise
            }
            .also { Data.exercises.addAll(it) }

        db.trainingDao().getCurrentTraining()?.apply {
            Data.trainingId = trainingId
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
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}