package org.scp.gymlog.service

import android.content.Context
import androidx.preference.PreferenceManager
import org.json.JSONException
import org.json.JSONObject
import org.scp.gymlog.room.AppDatabase
import org.scp.gymlog.room.entities.*
import org.scp.gymlog.service.dumper.DumperDataStructure
import org.scp.gymlog.util.Constants
import org.scp.gymlog.util.Data
import java.io.*
import java.util.stream.Collectors.joining

class DataBaseDumperService {

    companion object {
        const val OUTPUT = "output.json"
    }

    @Throws(JSONException::class, IOException::class)
    fun save(context: Context, fos: FileOutputStream?, database: AppDatabase): InputStream? {
        val dataStructure = DumperDataStructure()

        val bits = database.bitDao().getAllFromAllGyms()
        val trainings = database.trainingDao().getAll()
        dataStructure.extractNotes(bits, trainings)

        dataStructure.prefs = PreferenceManager.getDefaultSharedPreferences(context)
        dataStructure.gyms = database.gymDao().getAll().map { it.name }
        dataStructure.exercises = database.exerciseDao().getAll()
        dataStructure.variations = database.variationDao().getAllFromAllGyms()
        dataStructure.primaries = database.exerciseMuscleCrossRefDao().getAll()
        dataStructure.secondaries = database.exerciseMuscleCrossRefDao().getAllSecondaryMuscles()
        dataStructure.trainings = trainings
        dataStructure.bits = bits

        val outputStream = fos ?: ByteArrayOutputStream()
        val writer = PrintWriter(BufferedWriter(OutputStreamWriter(outputStream)), true)
        writer.println(dataStructure.jsonObject)
        writer.close()
        return (outputStream as? ByteArrayOutputStream)
            ?.let { ByteArrayInputStream(it.toByteArray()) }
    }

    @Throws(JSONException::class, IOException::class)
    fun load(context: Context, inputStream: InputStream, database: AppDatabase) {
        BufferedReader(InputStreamReader(inputStream)).use { br ->
            val dataStructure = br.lines().collect(joining(""))
                .let(::JSONObject)
                .let(::DumperDataStructure)

            // PREFS
            prefs(context, dataStructure.jsonObject.getJSONObject("prefs"))

            // GYMS
            database.gymDao().clear()
            dataStructure.gyms
                .map { GymEntity(name = it) }
                .also { database.gymDao().insertAll(it) }

            // EXERCISES:
            val exercisesIdMap = mutableMapOf<Int, Int>()
            val addedExercisesIds = mutableListOf<Int>()
            var newIds = Data.exercises.size

            dataStructure.exercises.forEach { exercise ->
                val matchingEx = Data.exercises.find {
                        it.name.equals(exercise.name, ignoreCase = true )
                    }
                if (matchingEx != null) {
                    exercisesIdMap[exercise.exerciseId] = matchingEx.id
                    exercise.exerciseId = matchingEx.id
                    database.exerciseDao().update(exercise)
                } else {
                    exercisesIdMap[exercise.exerciseId] = ++newIds
                    exercise.exerciseId = newIds
                    addedExercisesIds.add(newIds)
                    database.exerciseDao().insert(exercise)
                }
            }

            // PRIMARY AND SECONDARY MUSCLES
            dataStructure.primaries
                .onEach { it.exerciseId = exercisesIdMap[it.exerciseId]!! }
                .filter { addedExercisesIds.contains(it.exerciseId) }
                .also { database.exerciseMuscleCrossRefDao().insertAll(it) }

            dataStructure.secondaries
                .onEach { it.exerciseId = exercisesIdMap[it.exerciseId]!! }
                .filter { addedExercisesIds.contains(it.exerciseId) }
                .also { database.exerciseMuscleCrossRefDao().insertAllSecondaries(it) }

            // VARIATION
            val variationsIdMap = mutableMapOf<Int, Int>()
            val variationsXExerciseIdMap = mutableMapOf<Int, Int>()
            newIds = Data.exercises.flatMap { it.variations }.count()

            dataStructure.variations
                .forEach { variation ->
                    val exerciseId = exercisesIdMap[variation.exerciseId]!!
                    val matchingEx = Data.exercises.find { it.id == exerciseId }
                    val matchingVa = matchingEx?.variations?.find {
                        it.default && variation.def ||
                        it.name.equals(variation.name, ignoreCase = true )
                    }

                    if (matchingVa != null) {
                        variationsIdMap[variation.variationId] = matchingVa.id
                        variationsXExerciseIdMap[matchingVa.id] = exerciseId
                        variation.variationId = matchingVa.id
                        variation.exerciseId = exerciseId

                        database.variationDao().update(variation)
                    } else {
                        variationsIdMap[variation.variationId] = ++newIds
                        variationsXExerciseIdMap[newIds] = exerciseId
                        variation.variationId = newIds
                        variation.exerciseId = exerciseId

                        database.variationDao().insert(variation)
                    }
                }

            // TRAININGS
            val trainingOrig = mutableListOf<Int>()
            val trainingsIdMap = mutableMapOf<Int, Int>()
            val trainings = dataStructure.trainings
            for (trainingEntity in trainings) {
                trainingOrig.add(trainingEntity.trainingId)
                trainingEntity.trainingId = 0
            }
            val newTrIds = database.trainingDao().insertAll(trainings)
            for (i in newTrIds.indices) {
                trainingsIdMap[trainingOrig[i]] = newTrIds[i].toInt()
            }

            // BITS
            val bits = dataStructure.bits
                .onEach {
                    it.variationId = variationsIdMap[it.variationId]!!
                    it.trainingId = trainingsIdMap[it.trainingId]!!
                }
                .also { database.bitDao().insertAll(it) }

            // Update most recent bit to exercises
            database.exerciseDao().getAll()
                .filter { exerciseEntity ->
                    exerciseEntity.lastTrained = bits
                        .filter { variationsXExerciseIdMap[it.variationId] == exerciseEntity.exerciseId }
                        .ifEmpty { return@filter false }
                        .map { it.timestamp }
                        .fold(Constants.DATE_ZERO) { acc, value -> if (value > acc) value else acc }

                    exerciseEntity.lastTrained > Constants.DATE_ZERO
                }
                .also { database.exerciseDao().update(*it.toTypedArray()) }
        }
    }

    @Throws(JSONException::class)
    private fun prefs(context: Context, prefs: JSONObject) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = preferences.edit()
        val keys = prefs.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            when (prefs[key]) {
                is String -> { editor.putString(key, prefs.getString(key)) }
                is Int -> { editor.putInt(key, prefs.getInt(key)) }
                is Boolean -> { editor.putBoolean(key, prefs.getBoolean(key)) }
            }
        }
        editor.apply()
    }
}