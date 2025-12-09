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
        dataStructure.setTrainingsAndUpdateTimes(trainings, bits)
        progressNotify.replaceRange(30, 90, "Logs")
        dataStructure.setBits(bits)

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

            // Persist Muscles and bars:
            InitialDataService.createDefaults(database)

            // GYMS
            progressNotify.replaceRange(2, 3, "Gyms")
            database.gymDao().clear()
            dataStructure.gyms
                .map { GymEntity(name = it) }
                .also { database.gymDao().insertAll(it) }

            // EXERCISES:
            progressNotify.replaceRange(3, 7, "Exercises")
            database.exerciseDao().insertAll(dataStructure.exercises)

            // PRIMARY AND SECONDARY MUSCLES
            progressNotify.replaceRange(7, 20, "Variations")
            database.exerciseMuscleCrossRefDao().insertAll(dataStructure.primaries)
            database.exerciseMuscleCrossRefDao().insertAllSecondaries(dataStructure.secondaries)

            // VARIATION
            database.variationDao().insertAll(dataStructure.variations)

            // TRAININGS
            progressNotify.replaceRange(20, 30, "Trainings")
            val trainings = dataStructure.getTrainings()
            val trainingsIdMap: Map<Int, Int> = trainings
                .map {
                    val oldId = it.trainingId
                    it.trainingId = 0 // create new id on database
                    oldId
                }
                .zip( // creates "oldId -> newId"
                    database.trainingDao().insertAll(trainings)
                        .map { it.toInt() }
                )
                .toMap()


            // BITS
            progressNotify.replaceRange(30, 90, "Logs")
            val bits = dataStructure.getBits()
                .onEach { it.trainingId = trainingsIdMap[it.trainingId]!! }
                .also {
                    progressNotify.update(100, "Database inserting logs")
                    database.bitDao().insertAll(it)
                }

            // Update most recent bit to exercises
            val variationExerciseId = dataStructure.variations
                .associate { it.variationId to it.exerciseId }

            val exercisesTimestamp = bits
                .groupBy { variationExerciseId[it.variationId]!! }
                .mapValues { (_, bits) -> bits.maxOf { it.timestamp } }

            database.exerciseDao().getAll()
                .filter {
                    it.lastTrained = exercisesTimestamp[it.exerciseId] ?: return@filter false
                    it.lastTrained > Constants.DATE_ZERO
                }
                .also(database.exerciseDao()::update)

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

    companion object {
        const val OUTPUT = "output.json"
    }
}