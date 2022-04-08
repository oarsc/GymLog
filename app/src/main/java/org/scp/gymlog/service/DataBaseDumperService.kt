package org.scp.gymlog.service

import android.content.Context
import androidx.preference.PreferenceManager
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.scp.gymlog.exceptions.LoadException
import org.scp.gymlog.model.Exercise
import org.scp.gymlog.room.AppDatabase
import org.scp.gymlog.room.entities.*
import org.scp.gymlog.util.Constants
import org.scp.gymlog.util.Data
import org.scp.gymlog.util.JsonUtils
import java.io.*
import java.util.stream.Collectors
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
            JsonUtils.forEachObject(bits) { bit: JSONObject ->
                val id = bit.getInt("exerciseId")
                val exercise = Data.getExercise(id)
                bit.remove("exerciseId")
                bit.put("exerciseName", exercise.name)
                if (bit.getBoolean("kilos")) bit.remove("kilos")
                if (bit.getString("note").isEmpty()) bit.remove("note")
                if (bit.has("variationId")) {
                    val varId = bit.getInt("variationId")
                    bit.remove("variationId")

                    val variation = exercise.variations.find { variation -> variation.id == varId }
                        ?: throw LoadException("Can't find variation")

                    bit.put("variation", variation.name)
                }
            }
        } catch (e: JSONException) {
            throw LoadException("", e)
        }
        return bits
    }

    private fun convertToJSONArray(list: List<Any>): JSONArray {
        return list.stream()
            .map { obj: Any -> JsonUtils.jsonify(obj) }
            .collect(JsonUtils.collector())
    }

    @Throws(JSONException::class, IOException::class)
    fun load(context: Context, inputStream: InputStream, database: AppDatabase) {
        BufferedReader(InputStreamReader(inputStream)).use { br ->
            val line = br.lines().collect(Collectors.joining(""))
            val obj = JSONObject(line)

            // PREFS
            prefs(context, obj.getJSONObject("prefs"))

            // EXERCISES:
            val exercises = exercises(obj.getJSONArray("exercises"))
            val exercisesIdMap: MutableMap<Int, Int> = HashMap()
            val currentExercises = Data.exercises
            val addedIds: MutableList<Int> = ArrayList()
            var newIds = currentExercises.size
            for (ent in exercises) {
                val matchingEx = currentExercises.find { ex: Exercise ->
                        ex.name.equals(ent.name, ignoreCase = true )
                    }
                if (matchingEx != null) {
                    exercisesIdMap[ent.exerciseId] = matchingEx.id
                    ent.exerciseId = matchingEx.id
                    database.exerciseDao().update(ent)
                } else {
                    exercisesIdMap[ent.exerciseId] = ++newIds
                    ent.exerciseId = newIds
                    addedIds.add(newIds)
                    database.exerciseDao().insert(ent)
                }
            }
            val primaryMuscles = primaryMuscles(obj.getJSONArray("primaries"))
            for (primaryMuscle in primaryMuscles) {
                primaryMuscle.exerciseId = exercisesIdMap[primaryMuscle.exerciseId]!!
            }
            database.exerciseMuscleCrossRefDao().insertAll(
                primaryMuscles
                    .filter { entity: ExerciseMuscleCrossRef ->
                        addedIds.contains(entity.exerciseId)
                    }
            )

            val secondaryMuscles = secondaryMuscles(obj.getJSONArray("secondaries"))
            for (secondaryMuscle in secondaryMuscles) {
                secondaryMuscle.exerciseId = exercisesIdMap[secondaryMuscle.exerciseId]!!
            }
            database.exerciseMuscleCrossRefDao().insertAllSecondaries(
                secondaryMuscles
                    .filter { entity: SecondaryExerciseMuscleCrossRef ->
                        addedIds.contains(entity.exerciseId)
                    }
            )

            val trainingOrig: MutableList<Int> = ArrayList()
            val trainingsIdMap: MutableMap<Int, Int> = HashMap()
            val trainings = trainings(obj.getJSONArray("trainings"))
            for (trainingEntity in trainings) {
                trainingOrig.add(trainingEntity.trainingId)
                trainingEntity.trainingId = 0
            }
            val newTrIds = database.trainingDao().insertAll(trainings)
            for (i in newTrIds.indices) {
                trainingsIdMap[trainingOrig[i]] = newTrIds[i].toInt()
            }
            val variations: MutableMap<String, VariationEntity> =
                HashMap()
            val bits = bits(obj.getJSONArray("bits"), exercises, variations)
            database.variationDao()
                .insertAll(ArrayList(variations.values))
            for (bit in bits) {
                bit.trainingId = trainingsIdMap[bit.trainingId]!!
            }
            database.bitDao().insertAll(bits)

            // Update most recent bit to exercises
            //*
            database.exerciseDao().getAll()
                .filter { ex -> bits.any { bit: BitEntity -> bit.exerciseId == ex.exerciseId } }
                .onEach { exerciseEntity ->
                    exerciseEntity.lastTrained = bits
                        .filter { bitEntity -> bitEntity.exerciseId == exerciseEntity.exerciseId }
                        .map { bitEntity -> bitEntity.timestamp }
                        .fold(Constants.DATE_ZERO) { acc, value -> if (value > acc) value else acc }
                }
                .filter { exerciseEntity -> exerciseEntity.lastTrained > Constants.DATE_ZERO }
                .forEach { ex -> database.exerciseDao().update(ex) }
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
            exercises: List<ExerciseEntity>,
            variations: MutableMap<String, VariationEntity>): List<BitEntity> {

        JsonUtils.forEachObject(list) { bit: JSONObject ->
            val name = bit.getString("exerciseName")
            /*
            val id = Data.getInstance().exercises
                .filter { e -> e.name == name }
                .map { e -> e.id }
                .getOrElse(0) { throw LoadException("Couldn't find exercise: $name") }
            */

            val id = exercises
                .filter { e -> e.name == name }
                .map { e -> e.exerciseId }
                .getOrElse(0) { throw LoadException("Couldn't find exercise: $name") }

            bit.put("exerciseId", id)
            if (!bit.has("kilos")) bit.put("kilos", true)
            if (!bit.has("note")) bit.put("note", "")
            val variation = extractEntity(bit, variations)
            if (variation != null) {
                bit.put("variationId", variation.variationId)
            }
        }
        return convertToObject(list, BitEntity::class)
    }

    @Throws(JSONException::class)
    private fun extractEntity(
        bit: JSONObject,
        variations: MutableMap<String, VariationEntity>
    ): VariationEntity? {
        if (!bit.has("variation")) {
            return null
        }
        val key = bit.getInt("exerciseId").toString() + bit.getString("variation")
        if (!variations.containsKey(key)) {
            val variation = VariationEntity()
            variation.exerciseId = bit.getInt("exerciseId")
            variation.name = bit.getString("variation")
            variation.variationId = variations.size + 1
            variations[key] = variation
        }
        return variations[key]
    }

    @Throws(JSONException::class)
    private fun <T : Any> convertToObject(list: JSONArray, cls: KClass<T>): List<T> {
        return JsonUtils.mapObject(list) { json -> JsonUtils.objectify(json, cls) }
    }
}