package org.scp.gymlog.service.dumper

import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import org.scp.gymlog.room.entities.BitEntity
import org.scp.gymlog.room.entities.ExerciseEntity
import org.scp.gymlog.room.entities.ExerciseMuscleCrossRef
import org.scp.gymlog.room.entities.SecondaryExerciseMuscleCrossRef
import org.scp.gymlog.room.entities.TrainingEntity
import org.scp.gymlog.room.entities.VariationEntity
import org.scp.gymlog.util.JsonUtils.jsonify
import org.scp.gymlog.util.JsonUtils.map
import org.scp.gymlog.util.JsonUtils.objectify
import org.scp.gymlog.util.JsonUtils.toJsonArray
import kotlin.reflect.KClass

class DumperDataStructure(
    val jsonObject: JSONObject = JSONObject()
) {
    var prefs: SharedPreferences
        get() = throw UnsupportedOperationException("Getter not implemented")
        set(value) {
            jsonObject.put(DumperDataStructure::prefs.name, JSONObject(value.all))
        }

    var gyms: List<String>
        get() = jsonObject.getJSONArray(DumperDataStructure::gyms.name).map(JSONArray::getString)
        set(value) {
            jsonObject.put(DumperDataStructure::gyms.name, value.toJsonArray)
        }

    var exercises: List<ExerciseEntity>
        get() = jsonObject.getJSONArray(DumperDataStructure::exercises.name)
            .transformToObject(ExerciseEntity::class)
        set(value) {
            jsonObject.put(DumperDataStructure::exercises.name, value.transformToJson())
        }

    var variations: List<VariationEntity>
        get() = jsonObject.getJSONArray(DumperDataStructure::variations.name)
            .transformToObject(VariationEntity::class)
        set(value) {
            jsonObject.put(DumperDataStructure::variations.name, value.transformToJson())
        }

    var primaries: List<ExerciseMuscleCrossRef>
        get() = jsonObject.getJSONArray(DumperDataStructure::primaries.name)
            .transformToObject(ExerciseMuscleCrossRef::class)
        set(value) {
            jsonObject.put(DumperDataStructure::primaries.name, value.transformToJson())
        }

    var secondaries: List<SecondaryExerciseMuscleCrossRef>
        get() = jsonObject.getJSONArray(DumperDataStructure::secondaries.name)
            .transformToObject(SecondaryExerciseMuscleCrossRef::class)
        set(value) {
            jsonObject.put(DumperDataStructure::secondaries.name, value.transformToJson())
        }

    var trainings: List<TrainingEntity>
        get() {
            val trainings = jsonObject.getJSONArray(DumperDataStructure::trainings.name)

            trainings.loopAll {
                transformNoteToString()
            }

            return trainings.transformToObject(TrainingEntity::class)
        }
        set(value) {
            val trainings = value.transformToJson()

            trainings.loopAll {
                transformNoteToIdx()
            }

            jsonObject.put(DumperDataStructure::trainings.name, trainings)
        }

    var bits: List<BitEntity>
        get() {
            val bits = jsonObject.getJSONArray(DumperDataStructure::bits.name)

            bits.loopAll {
                transformNoteToString()
                addFieldIfEmpty(
                    BitEntity::superSet.name,
                    BitEntity::kilos.name,
                    BitEntity::instant.name
                ) {
                    when(it) {
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

            bits.loopAll {
                transformNoteToIdx()
                removeFieldIf(
                    BitEntity::superSet.name,
                    BitEntity::kilos.name,
                    BitEntity::instant.name
                ) {
                    when(it) {
                        BitEntity::superSet.name -> getInt(it) <= 0
                        BitEntity::kilos.name -> getBoolean(it)
                        BitEntity::instant.name -> !getBoolean(it)
                        else -> throw IllegalStateException("Can't chose removal for $it")
                    }
                }
            }

            jsonObject.put(DumperDataStructure::bits.name, bits)
        }

    private var notes: List<String>

    init {
        this.notes =
            if (jsonObject.has(DumperDataStructure::notes.name)) {
                jsonObject.getJSONArray(DumperDataStructure::notes.name).map(JSONArray::getString)
            } else {
                emptyList()
            }
    }

    fun extractNotes(bits: List<BitEntity>, trainings: List<TrainingEntity>) {
        val notes = bits.map { it.note }.toSet() + trainings.map { it.note }.toSet()
        this.notes = notes.filter { it.isNotBlank() }.sorted()
        jsonObject.put(DumperDataStructure::notes.name, this.notes.toJsonArray)
    }

    private fun JSONObject.transformNoteToString() {
        if (has("n")) {
            val noteIdx = getInt("n")
            remove("n")
            put("note", notes[noteIdx])
        } else {
            put("note", "")
        }
    }

    private fun JSONObject.transformNoteToIdx() {
        if (has("note")) {
            val note = getString("note")
            remove("note")
            if (note.isNotBlank()) {
                val idx = notes.indexOf(note)
                if (idx < 0) throw IllegalStateException("Wrong note index \"$note\"")
                put("n", idx)
            }
        }
    }

    private fun List<Any>.transformToJson(): JSONArray =
        this.map { it.jsonify() }
        .toJsonArray

    private fun <T : Any> JSONArray.transformToObject(cls: KClass<T>): List<T> =
        this.map(JSONArray::getJSONObject)
            .map { it.objectify(cls) }

    private fun JSONArray.loopAll(
        function: JSONObject.() -> Unit
    ) {
        this
            .map(JSONArray::getJSONObject)
            .forEach { it.function() }
    }

    private fun JSONObject.removeFieldIf(
        vararg fields: String,
        predicate: (String) -> Boolean
    ) {
        fields.forEach { field ->
            if (predicate(field)) {
                remove(field)
            }
        }
    }

    private fun JSONObject.addFieldIfEmpty(
        vararg fields: String,
        emtpyValueGenerator: (String) -> Any
    ) {
        fields.forEach { field ->
            if (!has(field)) {
                put(field, emtpyValueGenerator(field))
            }
        }
    }
}
