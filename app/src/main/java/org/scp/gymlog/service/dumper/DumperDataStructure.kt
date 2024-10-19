package org.scp.gymlog.service.dumper

import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import org.scp.gymlog.room.entities.BitEntity
import org.scp.gymlog.room.entities.BitEntity.BitEntityWithNotes
import org.scp.gymlog.room.entities.ExerciseEntity
import org.scp.gymlog.room.entities.ExerciseMuscleCrossRef
import org.scp.gymlog.room.entities.NoteEntity
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
            return trainings.transformToObject(TrainingEntity::class)
        }
        set(value) {
            val trainings = value.transformToJson()
            jsonObject.put(DumperDataStructure::trainings.name, trainings)
        }

    var bits: List<BitEntityWithNotes>
        get() {
            val bits = jsonObject.getJSONArray(DumperDataStructure::bits.name)

            return bits.map(JSONArray::getJSONObject)
                .map { obj ->
                    obj.addDefaultValues(
                        BitEntity::superSet.name to 0,
                        BitEntity::kilos.name to true,
                        BitEntity::instant.name to false
                    )
                    obj.objectify(BitEntity::class)
                        .let {
                            BitEntityWithNotes().apply {
                                bit = it
                                if (obj.has("n")) {
                                    notes = obj.getString("n")
                                        .split(",")
                                        .map(String::toInt)
                                        .map { NoteEntity().apply {
                                            noteId = it + 1
                                            content = notesIndexes[it]
                                        }}
                                }
                            }
                        }
                }
        }
        set(value) {
            val content = value.map { entity ->
                val json = entity.bit!!.jsonify()

                if (entity.notes.isNotEmpty()) {
                    json.put("n",
                        entity.notes
                            .map { it.content }
                            .map { notesIndexes.indexOf(it) }
                            .joinToString(",")
                    )
                }

                json.removeFieldIf(
                    BitEntity::superSet.name to { json.getInt(it) <= 0 /* value is <= 0 */},
                    BitEntity::kilos.name to { json.getBoolean(it) /* value is true */ },
                    BitEntity::instant.name to { !json.getBoolean(it) /* value is false */}
                )

                json
            }

            jsonObject.put(DumperDataStructure::bits.name, content.toJsonArray)
        }

    private val notesIndexes =
        if (jsonObject.has(DumperDataStructure::notes.name)) {
            jsonObject.getJSONArray(DumperDataStructure::notes.name).map(JSONArray::getString)
        } else {
            emptyList()
        }.toMutableList()

    var notes: List<NoteEntity>
        get() {
            val bitNotes = jsonObject.getJSONArray(DumperDataStructure::notes.name)
            return bitNotes.map { value, index ->
                NoteEntity().apply {
                    noteId = index + 1
                    content = value.getString(index)
                }
            }
        }
        set(value) {
            value
                .map { it.content }
                .distinct()
                .filter(String::isNotBlank)
                .sorted()
                .also {
                    notesIndexes.clear()
                    notesIndexes.addAll(it)
                    jsonObject.put(DumperDataStructure::notes.name, it.toJsonArray)
                }
        }

    private fun List<Any>.transformToJson(): JSONArray =
        this.map { it.jsonify() }
        .toJsonArray

    private fun <T : Any> JSONArray.transformToObject(cls: KClass<T>): List<T> =
        this.map(JSONArray::getJSONObject)
            .map { it.objectify(cls) }

    private fun JSONArray.loopAll(
        function: JSONObject.(Int) -> Unit
    ) {
        this
            .map(JSONArray::getJSONObject)
            .forEachIndexed { idx, it -> it.function(idx) }
    }

    private fun JSONObject.removeFieldIf(
        vararg fields: Pair<String, (String) -> Boolean>,
    ) {
        fields.forEach { (field, predicate) ->
            if (predicate(field)) {
                remove(field)
            }
        }
    }

    private fun JSONObject.addDefaultValues(vararg fields: Pair<String, Any>) {
        fields.forEach { (field, value) ->
            if (!has(field)) put(field, value)
        }
    }
}
