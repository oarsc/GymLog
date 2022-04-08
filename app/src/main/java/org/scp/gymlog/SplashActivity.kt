package org.scp.gymlog

import android.R
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
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
import org.scp.gymlog.room.entities.TrainingEntity
import org.scp.gymlog.service.InitialDataService
import org.scp.gymlog.service.NotificationService
import org.scp.gymlog.ui.main.MainActivity
import org.scp.gymlog.util.Data
import org.scp.gymlog.util.WeightUtils

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private val initialDataService = InitialDataService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        if (preferences.getBoolean("nightTheme", false) &&
                AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_YES) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            recreate()

        } else {
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

    private fun loadData(db: AppDatabase) {
        Data.bars.clear()
        db.barDao().getAll()
            .map { entity: BarEntity? -> Bar(entity!!) }
            .forEach { e: Bar -> Data.bars.add(e) }

        Data.exercises.clear()
        db.exerciseDao().getAllWithMusclesAndVariations()
            .map { x: WithMusclesAndVariations ->
                val exercise = Exercise(x.exercise!!)
                x.primaryMuscles!!
                    .map(MuscleEntity::muscleId)
                    .map { id: Int ->
                        Data.muscles.stream()
                            .filter { group: Muscle -> group.id == id }
                            .findFirst()
                            .orElseThrow { LoadException("Muscle $id not found in local structure") }
                    }
                    .forEach { muscle -> exercise.primaryMuscles.add(muscle) }

                x.secondaryMuscles!!
                    .map(MuscleEntity::muscleId)
                    .map { id: Int ->
                        Data.muscles.stream()
                            .filter { group: Muscle -> group.id == id }
                            .findFirst()
                            .orElseThrow { LoadException("Muscle $id not found in local structure") }
                    }
                    .forEach { muscle -> exercise.secondaryMuscles.add(muscle) }

                x.variations!!
                    .map { variationEntity -> Variation(variationEntity) }
                    .forEach { variation -> exercise.variations.add(variation) }
                exercise
            }
            .forEach { exercise -> Data.exercises.add(exercise) }

        db.trainingDao().getCurrentTraining()
            .ifPresent { training: TrainingEntity -> Data.trainingId = training.trainingId }
    }

    private fun goMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("loaded", true)
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        finish()
    }
}