package org.scp.gymlog.service

import android.content.res.AssetManager
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.scp.gymlog.exceptions.LoadException
import org.scp.gymlog.model.Bar
import org.scp.gymlog.model.Weight
import org.scp.gymlog.model.WeightSpecification
import org.scp.gymlog.room.AppDatabase
import org.scp.gymlog.room.entities.ExerciseEntity
import org.scp.gymlog.room.entities.ExerciseMuscleCrossRef
import org.scp.gymlog.room.entities.SecondaryExerciseMuscleCrossRef
import org.scp.gymlog.util.Constants
import org.scp.gymlog.util.Data
import org.scp.gymlog.util.JsonUtils
import java.io.IOException
import java.math.BigDecimal

class InitialDataService {
    fun persist(assets: AssetManager, db: AppDatabase) {
        persistMuscles(Data, db)
        createAndPersistBars(Data, db)
        loadExercises(assets, db)
    }

    companion object {
        private fun persistMuscles(data: Data, db: AppDatabase) {
            db.muscleDao().insertAll(
                data.muscles.map { muscle -> muscle.toEntity() })
        }

        private fun createAndPersistBars(data: Data, db: AppDatabase) {
            val bars = data.bars
            bars.clear()
            var barId = 0
            listOf(
                Bar(++barId, Weight(BigDecimal("7.5"), true)),
                Bar(++barId, Weight(BigDecimal("10"), true)),
                Bar(++barId, Weight(BigDecimal("12"), true)),
                Bar(++barId, Weight(BigDecimal("15"), true)),
                Bar(++barId, Weight(BigDecimal("20"), true)),
                Bar(++barId, Weight(BigDecimal("25"), true)),
            ).forEach { b: Bar -> bars.add(b) }

            db.barDao().insertAll(
                data.bars.map { bar -> bar.toEntity() })
        }

        private fun loadExercises(assets: AssetManager, db: AppDatabase) {
            val exercisesArray = assetJsonArrayFile(assets, "initialData.json")
            val exerciseDao = db.exerciseDao()
            val exXmuscleDao = db.exerciseMuscleCrossRefDao()
            try {
                JsonUtils.forEachObject(exercisesArray) { exerciseObj: JSONObject ->
                    val ex = ExerciseEntity()
                    ex.image = exerciseObj.getString("tag")
                    ex.name = exerciseObj.getString("name")
                    ex.requiresBar = exerciseObj.getBoolean("bar")
                    if (ex.requiresBar) {
                        ex.lastBarId = 4 // 20kg
                    }
                    ex.lastTrained = Constants.DATE_ZERO
                    ex.lastWeightSpec = WeightSpecification.NO_BAR_WEIGHT
                    ex.exerciseId = exerciseDao.insert(ex).toInt()

                    val muscle1Links = JsonUtils.mapInt(
                            exerciseObj.getJSONArray("primary")
                        ) { muscleId: Int ->
                            val exXmuscle = ExerciseMuscleCrossRef()
                            exXmuscle.exerciseId = ex.exerciseId
                            exXmuscle.muscleId = muscleId
                            exXmuscle
                        }
                    exXmuscleDao.insertAll(muscle1Links)

                    val muscle2Links = JsonUtils.mapInt(
                            exerciseObj.getJSONArray("secondary")
                        ) { muscleId: Int ->
                            val exXmuscle = SecondaryExerciseMuscleCrossRef()
                            exXmuscle.exerciseId = ex.exerciseId
                            exXmuscle.muscleId = muscleId
                            exXmuscle
                        }
                    exXmuscleDao.insertAllSecondaries(muscle2Links)
                }
            } catch (e: JSONException) {
                throw LoadException("Unable to load initial exercises")
            }
        }

        private fun assetJsonArrayFile(assets: AssetManager, fileName: String): JSONArray {
            return try {
                val file = assets.open(fileName)
                val formArray = ByteArray(file.available())
                file.read(formArray)
                file.close()
                JSONArray(String(formArray))
            } catch (e: JSONException) {
                throw LoadException("Unable to load file $fileName")
            } catch (e: IOException) {
                throw LoadException("Unable to load file $fileName")
            }
        }
    }
}