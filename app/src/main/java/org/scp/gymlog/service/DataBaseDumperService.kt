package org.scp.gymlog.service

import android.content.Context
import androidx.preference.PreferenceManager
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.scp.gymlog.exceptions.LoadException
import org.scp.gymlog.room.AppDatabase
import org.scp.gymlog.room.entities.*
import org.scp.gymlog.util.Constants
import org.scp.gymlog.util.Data
import org.scp.gymlog.util.JsonUtils
import org.scp.gymlog.util.JsonUtils.map
import org.scp.gymlog.util.JsonUtils.objectify
import org.scp.gymlog.util.JsonUtils.toJsonArray
import java.io.*
import java.util.stream.Collectors.joining
import kotlin.reflect.KClass

class DataBaseDumperService {

    companion object {
        const val OUTPUT = "output.json"
    }

    @Throws(JSONException::class, IOException::class)
    fun save(context: Context, fos: FileOutputStream, database: AppDatabase) {
        val obj = JSONObject()
        obj.put("prefs", prefs(context))
        obj.put("exercises", exercises(database))
        obj.put("variations", variations(database))
        obj.put("primaries", primaryMuscles(database))
        obj.put("secondaries", secondaryMuscles(database))
        obj.put("trainings", trainings(database))
        obj.put("bits", bits(database))
        val writer = PrintWriter(BufferedWriter(OutputStreamWriter(fos)), true)
        writer.println(obj.toString())
        writer.close()
    }

    private fun prefs(context: Context): JSONObject {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return JSONObject(preferences.all)
    }

    private fun exercises(database: AppDatabase): JSONArray {
        return convertToJSONArray(database.exerciseDao().getAll())
    }

    private fun variations(database: AppDatabase): JSONArray {
        return convertToJSONArray(database.variationDao().getAll())
    }

    private fun primaryMuscles(database: AppDatabase): JSONArray {
        return convertToJSONArray(database.exerciseMuscleCrossRefDao().getAll())
    }

    private fun secondaryMuscles(database: AppDatabase): JSONArray {
        return convertToJSONArray(database.exerciseMuscleCrossRefDao().getAllSecondaryMuscles())
    }

    private fun trainings(database: AppDatabase): JSONArray {
        return convertToJSONArray(database.trainingDao().getAll())
    }

    private fun bits(database: AppDatabase): JSONArray {
        val bits = convertToJSONArray(database.bitDao().getAll())
        try {
            bits.map(JSONArray::getJSONObject).forEach { bit: JSONObject ->
                if (bit.getBoolean("kilos")) bit.remove("kilos")
                if (bit.getString("note").isEmpty()) bit.remove("note")
            }
        } catch (e: JSONException) {
            throw LoadException("", e)
        }
        return bits
    }

    private fun convertToJSONArray(list: List<Any>): JSONArray {
        return list
            .map { obj: Any -> JsonUtils.jsonify(obj) }
            .toJsonArray
    }

    @Throws(JSONException::class, IOException::class)
    fun load(context: Context, inputStream: InputStream, database: AppDatabase) {
        BufferedReader(InputStreamReader(inputStream)).use { br ->
            val line = br.lines().collect(joining(""))
            val obj = JSONObject(line)

            // PREFS
            prefs(context, obj.getJSONObject("prefs"))

            // EXERCISES:
            val exercises = exercises(obj.getJSONArray("exercises"))
            val exercisesIdMap = mutableMapOf<Int, Int>()
            val addedExercisesIds = mutableListOf<Int>()
            var newIds = Data.exercises.size

            exercises.forEach { exercise ->
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
            primaryMuscles(obj.getJSONArray("primaries"))
                .onEach { it.exerciseId = exercisesIdMap[it.exerciseId]!! }
                .filter { addedExercisesIds.contains(it.exerciseId) }
                .also { database.exerciseMuscleCrossRefDao().insertAll(it) }

            secondaryMuscles(obj.getJSONArray("primaries"))
                .onEach { it.exerciseId = exercisesIdMap[it.exerciseId]!! }
                .filter { addedExercisesIds.contains(it.exerciseId) }
                .also { database.exerciseMuscleCrossRefDao().insertAllSecondaries(it) }

            // VARIATION
            val variationsIdMap = mutableMapOf<Int, Int>()
            val variationsXExerciseIdMap = mutableMapOf<Int, Int>()
            newIds = Data.exercises.flatMap { it.variations }.count()

            variations(obj.getJSONArray("variations"))
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
            val trainings = trainings(obj.getJSONArray("trainings"))
            for (trainingEntity in trainings) {
                trainingOrig.add(trainingEntity.trainingId)
                trainingEntity.trainingId = 0
            }
            val newTrIds = database.trainingDao().insertAll(trainings)
            for (i in newTrIds.indices) {
                trainingsIdMap[trainingOrig[i]] = newTrIds[i].toInt()
            }

            // BITS
            val bits = bits(obj.getJSONArray("bits"), variationsIdMap, variationsXExerciseIdMap)
            bits.onEach { it.trainingId = trainingsIdMap[it.trainingId]!! }
                .also { database.bitDao().insertAll(it) }

            // Update most recent bit to exercises
            //*
            database.exerciseDao().getAll()
                .filter { ex ->
                    bits.any { bit -> variationsXExerciseIdMap[bit.variationId] == ex.exerciseId }
                }
                .onEach { exerciseEntity ->
                    exerciseEntity.lastTrained = bits
                        .filter { bit -> variationsXExerciseIdMap[bit.variationId] == exerciseEntity.exerciseId }
                        .map { bit -> bit.timestamp }
                        .fold(Constants.DATE_ZERO) { acc, value -> if (value > acc) value else acc }
                }
                .filter { exerciseEntity -> exerciseEntity.lastTrained > Constants.DATE_ZERO }
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

    @Throws(JSONException::class)
    private fun exercises(list: JSONArray): List<ExerciseEntity> {
        return convertToObject( list, ExerciseEntity::class)
    }

    @Throws(JSONException::class)
    private fun variations(list: JSONArray): List<VariationEntity> {
        return convertToObject(list, VariationEntity::class)
    }

    @Throws(JSONException::class)
    private fun primaryMuscles(list: JSONArray): List<ExerciseMuscleCrossRef> {
        return convertToObject(list, ExerciseMuscleCrossRef::class)
    }

    @Throws(JSONException::class)
    private fun secondaryMuscles(list: JSONArray): List<SecondaryExerciseMuscleCrossRef> {
        return convertToObject(list, SecondaryExerciseMuscleCrossRef::class)
    }

    @Throws(JSONException::class)
    private fun trainings(list: JSONArray): List<TrainingEntity> {
        return convertToObject(list, TrainingEntity::class)
    }

    @Throws(JSONException::class)
    private fun bits(
            list: JSONArray,
            variationsIdMap: MutableMap<Int, Int>,
            variationsXExerciseIdMap: MutableMap<Int, Int>): List<BitEntity> {

        list.map(JSONArray::getJSONObject).forEach { bit: JSONObject ->
            val variationId = variationsIdMap[bit.get("variationId")]
            bit.put("variationId", variationId)
            bit.put("exerciseId", variationsXExerciseIdMap[variationId]!!)

            if (!bit.has("kilos")) bit.put("kilos", true)
            if (!bit.has("note")) bit.put("note", "")
        }
        return convertToObject(list, BitEntity::class)
    }

    @Throws(JSONException::class)
    private fun <T : Any> convertToObject(list: JSONArray, cls: KClass<T>): List<T> {
        return list.map(JSONArray::getJSONObject)
            .map { it.objectify(cls) }
    }
}