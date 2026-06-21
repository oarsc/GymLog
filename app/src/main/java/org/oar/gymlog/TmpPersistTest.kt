package org.oar.gymlog

import android.content.Context
import android.content.res.AssetManager
import org.json.JSONArray
import org.json.JSONException
import org.oar.gymlog.exceptions.LoadException
import org.oar.gymlog.room.AppDatabase
import org.oar.gymlog.room.entities.WeightPeriodEntity
import org.oar.gymlog.room.entities.WorkoutEntity
import org.oar.gymlog.room.entities.WorkoutExerciseEntity
import org.oar.gymlog.room.entities.WorkoutSetEntity
import org.oar.gymlog.util.JsonUtils.map
import java.io.IOException
import java.time.LocalDate

object TmpPersistTest {
    fun Context.persistTmpWorkouts(db: AppDatabase) {
        val workoutTestData = assets.fromJsonArrayFile("workoutTestData.json")
        db.persistWorkouts(workoutTestData)
        db.persistWeightPeriod()
    }

    private fun AppDatabase.persistWorkouts(workoutJsonArray: JSONArray) {
        val workoutDao = workoutDao()
        workoutJsonArray.map(JSONArray::getJSONObject).forEach {
            val workoutEntity = WorkoutEntity().apply {
                this.name = it.getString("name")
                this.color = it.optString("color", "")
            }
            val workoutId = workoutDao.insert(workoutEntity).toInt()
            persistWorkoutExercises(it.getJSONArray("exercises"), workoutId)
        }
    }

    private fun AppDatabase.persistWorkoutExercises(workoutExercisesJsonArray: JSONArray, workoutId: Int) {
        val workoutExerciseDao = workoutExerciseDao()
        workoutExercisesJsonArray.map(JSONArray::getJSONObject).forEach {
            val workoutExerciseEntity = WorkoutExerciseEntity().apply {
                this.workoutId = workoutId
                this.variationId = it.getInt("variation")
                this.superSet = it.optInt("superSet", 0)
            }
            val workoutExerciseId = workoutExerciseDao.insert(workoutExerciseEntity).toInt()
            persistWorkoutSets(it.getJSONArray("sets"), workoutExerciseId)
        }
    }

    private fun AppDatabase.persistWorkoutSets(workoutSetJsonArray: JSONArray, workoutExerciseId: Int) {
        val workoutSetDao = workoutSetDao()
        var order = 0

        workoutSetJsonArray.map(JSONArray::getJSONObject).forEach {
            val workoutSetEntity = WorkoutSetEntity().apply {
                this.workoutExerciseId = workoutExerciseId
                this.order = ++order
                this.totalWeight = it.optInt("totalWeight", 0)
                this.kilos = it.optBoolean("kilos", true)
                this.reps = it.optInt("reps", 0)
                this.note = it.optString("note", "")
                this.restTime = it.optInt("restTime", -1)
                this.warmUp = it.optBoolean("warmUp", false)
            }
            workoutSetDao.insert(workoutSetEntity)
        }
    }

    private fun AppDatabase.persistWeightPeriod() {
        val weightPeriodDao = weightPeriodDao()

        weightPeriodDao.insert(WeightPeriodEntity().apply {
            start = LocalDate.of(2025, 8, 16)
            end = LocalDate.of(2027, 8, 16)
            initialWeight = 8100
            initialBodyFatPercent = 900
            gainGramsPerWeek = 80
            loseGramsPerWeek = 350
            expectedMuscleGain = 300
        })
    }

    private fun AssetManager.fromJsonArrayFile(fileName: String): JSONArray = try {
        val file = this.open(fileName)
        val formArray = ByteArray(file.available())
        file.read(formArray)
        file.close()
        JSONArray(String(formArray))
    } catch (_: JSONException) {
        throw LoadException("Unable to load file $fileName")
    } catch (_: IOException) {
        throw LoadException("Unable to load file $fileName")
    }
}