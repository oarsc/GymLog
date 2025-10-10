package org.oar.gymlog.service

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import org.json.JSONException
import org.json.JSONObject
import org.oar.gymlog.room.AppDatabase
import org.oar.gymlog.room.entities.GymEntity
import org.oar.gymlog.service.dumper.DumperDataStructure
import org.oar.gymlog.ui.RangedProgress
import org.oar.gymlog.util.Constants
import org.oar.gymlog.util.Data
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.util.stream.Collectors.joining

class DataBaseDumperService {

    companion object {
        const val OUTPUT = "output.json"
    }

    @Throws(JSONException::class, IOException::class)
    fun save(context: Context, fos: FileOutputStream?, database: AppDatabase, progressNotify: RangedProgress): InputStream? {
        val dataStructure = DumperDataStructure(progressNotify = progressNotify)

        val bits = database.bitDao().getAllFromAllGyms()
        val trainings = database.trainingDao().getAll()
        dataStructure.extractNotes(bits, trainings)

        progressNotify.setRange(0, 2, "Preferences")
        dataStructure.prefs = PreferenceManager.getDefaultSharedPreferences(context)
        progressNotify.replaceRange(2, 3, "Gyms")
        dataStructure.gyms = database.gymDao().getAll().map { it.name }
        progressNotify.replaceRange(3, 7, "Exercises")
        dataStructure.exercises = database.exerciseDao().getAll()
        progressNotify.replaceRange(7, 20, "Variations")
        dataStructure.variations = database.variationDao().getAllFromAllGyms()
        dataStructure.primaries = database.exerciseMuscleCrossRefDao().getAll()
        dataStructure.secondaries = database.exerciseMuscleCrossRefDao().getAllSecondaryMuscles()
        progressNotify.replaceRange(20, 30, "Trainings")
        dataStructure.trainings = trainings
        progressNotify.replaceRange(30, 90, "Logs")
        dataStructure.bits = bits

        progressNotify.replaceRange(90, 100, "Writing file")
        val outputStream = fos ?: ByteArrayOutputStream()
        val writer = PrintWriter(BufferedWriter(OutputStreamWriter(outputStream)), true)
        writer.println(dataStructure.jsonObject)
        writer.close()
        val output =
            if (outputStream is ByteArrayOutputStream) ByteArrayInputStream(outputStream.toByteArray())
            else null
        progressNotify.removeRange()
        return output
    }

    @Throws(JSONException::class, IOException::class)
    fun load(context: Context, inputStream: InputStream, database: AppDatabase, progressNotify: RangedProgress) {
        BufferedReader(InputStreamReader(inputStream)).use { br ->
            val dataStructure = br.lines().collect(joining(""))
                .let { DumperDataStructure(JSONObject(it), progressNotify) }

            // PREFS
            progressNotify.setRange(0, 2, "Preferences")
            prefs(context, dataStructure.jsonObject.getJSONObject("prefs"))

            // GYMS
            progressNotify.replaceRange(2, 3, "Gyms")
            database.gymDao().clear()
            dataStructure.gyms
                .map { GymEntity(name = it) }
                .also { database.gymDao().insertAll(it) }

            // EXERCISES:
            progressNotify.replaceRange(3, 7, "Exercises")
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
            progressNotify.replaceRange(7, 20, "Variations")
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
            progressNotify.replaceRange(20, 30, "Trainings")
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
            progressNotify.replaceRange(30, 90, "Logs")
            val bits = dataStructure.bits
                .onEach {
                    it.variationId = variationsIdMap[it.variationId]!!
                    it.trainingId = trainingsIdMap[it.trainingId]!!
                }
                .also {
                    progressNotify.update(100, "Database inserting logs")
                    database.bitDao().insertAll(it)
                }

            // Update most recent bit to exercises
            val mostRecents = bits
                .groupBy { it.variationId }
                .mapNotNull { (variationId, group) ->
                    variationsXExerciseIdMap[variationId]?.let { exerciseId ->
                        exerciseId to group.maxOf { it.timestamp }
                    }
                }
                .toMap()

            val exercises = database.exerciseDao().getAll()
            exercises
                .filter { exerciseEntity ->
                    exerciseEntity.lastTrained = mostRecents[exerciseEntity.exerciseId] ?: return@filter false
                    exerciseEntity.lastTrained > Constants.DATE_ZERO
                }
                .also { database.exerciseDao().update(*it.toTypedArray()) }

            progressNotify.removeRange()
        }
    }

    @Throws(JSONException::class)
    private fun prefs(context: Context, prefs: JSONObject) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val keys = prefs.keys()

        preferences.edit {
            while (keys.hasNext()) {
                val key = keys.next()
                when (prefs[key]) {
                    is String -> putString(key, prefs.getString(key))
                    is Int -> putInt(key, prefs.getInt(key))
                    is Boolean -> putBoolean(key, prefs.getBoolean(key))
                }
            }
        }
    }
}