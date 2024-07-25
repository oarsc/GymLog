package org.scp.gymlog.service.dumper.model

import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import org.scp.gymlog.room.entities.BitEntity
import org.scp.gymlog.room.entities.ExerciseEntity
import org.scp.gymlog.room.entities.ExerciseMuscleCrossRef
import org.scp.gymlog.room.entities.SecondaryExerciseMuscleCrossRef
import org.scp.gymlog.room.entities.TrainingEntity
import org.scp.gymlog.room.entities.VariationEntity
import org.scp.gymlog.util.JsonUtils
import org.scp.gymlog.util.JsonUtils.map
import org.scp.gymlog.util.JsonUtils.objectify
import org.scp.gymlog.util.JsonUtils.toJsonArray
import kotlin.reflect.KClass

class JsonDataStructure(
    val jsonObject: JSONObject = JSONObject()
) {
    var prefs: SharedPreferences
        get() = throw UnsupportedOperationException("Getter not implemented")
        set(value) {
            jsonObject.put(JsonDataStructure::prefs.name, JSONObject(value.all))
        }

    var gyms: List<String>
        get() = jsonObject.getJSONArray(JsonDataStructure::gyms.name).map(JSONArray::getString)
        set(value) {
            jsonObject.put(JsonDataStructure::gyms.name, value.toJsonArray)
        }

    var exercises: List<ExerciseEntity>
        get() = jsonObject.getJSONArray(JsonDataStructure::exercises.name).transformToObject(ExerciseEntity::class)
        set(value) {
            jsonObject.put(JsonDataStructure::exercises.name, value.transformToJson())
        }

    var variations: List<VariationEntity>
        get() = jsonObject.getJSONArray(JsonDataStructure::variations.name).transformToObject(VariationEntity::class)
        set(value) {
            jsonObject.put(JsonDataStructure::variations.name, value.transformToJson())
        }

    var primaries: List<ExerciseMuscleCrossRef>
        get() = jsonObject.getJSONArray(JsonDataStructure::primaries.name).transformToObject(ExerciseMuscleCrossRef::class)
        set(value) {
            jsonObject.put(JsonDataStructure::primaries.name, value.transformToJson())
        }

    var secondaries: List<SecondaryExerciseMuscleCrossRef>
        get() = jsonObject.getJSONArray(JsonDataStructure::secondaries.name).transformToObject(SecondaryExerciseMuscleCrossRef::class)
        set(value) {
            jsonObject.put(JsonDataStructure::secondaries.name, value.transformToJson())
        }

    var trainings: List<TrainingEntity>
        get() {
            val trainings = jsonObject.getJSONArray(JsonDataStructure::trainings.name).apply {
                addIfEmpty(TrainingEntity::note.name) { "" }
            }

            return trainings.transformToObject(TrainingEntity::class)
        }
        set(value) {
            val trainings = value.transformToJson()
                .apply {
                    removeIf(TrainingEntity::note.name) { getString(it).isBlank() }

                }
            jsonObject.put(JsonDataStructure::trainings.name, trainings)
        }

    var bits: List<BitEntity>
        get() {
            val bits = jsonObject.getJSONArray(JsonDataStructure::bits.name).apply {
                addIfEmpty(
                    BitEntity::note.name,
                    BitEntity::superSet.name,
                    BitEntity::kilos.name,
                    BitEntity::instant.name
                ) {
                    when(it) {
                        BitEntity::note.name -> ""
                        BitEntity::superSet.name -> 0
                        BitEntity::kilos.name -> true
                        BitEntity::instant.name -> false
                        else -> throw IllegalStateException("Can't set default value for $it")
                    }
                }
            }

            return bits.transformToObject(BitEntity::class)
        }
        set(value) {
            val bits = value.transformToJson()
                .apply {
                    removeIf(
                        BitEntity::note.name,
                        BitEntity::superSet.name,
                        BitEntity::kilos.name,
                        BitEntity::instant.name
                    ) {
                        when(it) {
                            BitEntity::note.name -> getString(it).isBlank()
                            BitEntity::superSet.name -> getInt(it) <= 0
                            BitEntity::kilos.name -> getBoolean(it)
                            BitEntity::instant.name -> !getBoolean(it)
                            else -> throw IllegalStateException("Can't chose removal for $it")
                        }
                    }
                }
            jsonObject.put(JsonDataStructure::bits.name, bits)
        }

    var notes: List<String> = emptyList()
        private set

    init {
        if (jsonObject.has(JsonDataStructure::notes.name)) {
            this.notes = jsonObject.getJSONArray(JsonDataStructure::notes.name).map(JSONArray::getString)
        } else {
            this.notes = emptyList()
        }
    }

    fun extractNotes(bits: List<BitEntity>, trainings: List<TrainingEntity>) {
        val notes = bits.map { it.note }.toSet() + trainings.map { it.note }.toSet()
        this.notes = notes.filter { it.isBlank() }.sorted()
//        jsonObject.put(JsonDataStructure::notes.name, this.notes.toJsonArray)
    }

    private fun List<Any>.transformToJson(): JSONArray =
        this.map { JsonUtils.jsonify(it) }
        .toJsonArray

    private fun <T : Any> JSONArray.transformToObject(cls: KClass<T>): List<T> =
        this.map(JSONArray::getJSONObject)
            .map { it.objectify(cls) }


    private fun JSONArray.removeIf(
        vararg fields: String,
        function: JSONObject.(String) -> Boolean
    ) {
        this
            .map(JSONArray::getJSONObject)
            .forEach { obj ->
                fields.forEach { field ->
                    if (obj.function(field)) {
                        obj.remove(field)
                    }
                }
            }
    }

    private fun JSONArray.addIfEmpty(
        vararg fields: String,
        function: (String) -> Any
    ) {
        this
            .map(JSONArray::getJSONObject)
            .forEach { obj ->
                fields.forEach { field ->
                    if (!obj.has(field)) {
                        obj.put(field, function(field))
                    }
                }
            }
    }
}
