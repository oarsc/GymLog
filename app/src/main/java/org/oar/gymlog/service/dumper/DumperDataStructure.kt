package org.oar.gymlog.service.dumper

import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import org.oar.gymlog.room.entities.BitEntity
import org.oar.gymlog.room.entities.ExerciseEntity
import org.oar.gymlog.room.entities.ExerciseMuscleCrossRef
import org.oar.gymlog.room.entities.SecondaryExerciseMuscleCrossRef
import org.oar.gymlog.room.entities.TrainingEntity
import org.oar.gymlog.room.entities.VariationEntity
import org.oar.gymlog.ui.RangedProgress
import org.oar.gymlog.util.JsonUtils.jsonify
import org.oar.gymlog.util.JsonUtils.map
import org.oar.gymlog.util.JsonUtils.objectify
import org.oar.gymlog.util.JsonUtils.toJsonArray
import java.time.LocalDateTime
import kotlin.reflect.KClass

class DumperDataStructure(
    val jsonObject: JSONObject = JSONObject(),
    val progressNotify: RangedProgress
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

    val trainings: List<TrainingEntity>
        get() {
            val trainings = jsonObject.getJSONArray(DumperDataStructure::trainings.name)

            val length = trainings.length().toDouble()
            progressNotify.setRange(0, 10)

            trainings.loopAllIndexed { index ->
                transformNoteToString()
                if (index % 25 == 0) {
                    progressNotify.update((index / length * 100).toInt())
                }
            }

            progressNotify.replaceRange(10, 100)

            return trainings.transformToObject(TrainingEntity::class, progressNotify).apply {
                progressNotify.removeRange()
            }
        }

    var bits: List<BitEntity>
        get() {
            val bits = jsonObject.getJSONArray(DumperDataStructure::bits.name)

            val length = bits.length().toDouble()
            progressNotify.setRange(0, 10)

            bits.loopAllIndexed { index ->
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
                if (index % 100 == 0) {
                    progressNotify.update((index / length * 100).toInt())
                }
            }

            progressNotify.replaceRange(10, 100)

            return bits.transformToObject(BitEntity::class, progressNotify).apply {
                progressNotify.removeRange()
            }
        }
        set(value) {
            progressNotify.setRange(0, 90)
            val bits = value.transformToJson(progressNotify)

            val length = bits.length().toDouble()

            progressNotify.replaceRange(90, 100)

            bits.loopAllIndexed { index ->
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
                if (index % 100 == 0) {
                    progressNotify.update((index / length * 100).toInt())
                }
            }

            progressNotify.removeRange()

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

    fun setTrainingsAndUpdateTimes(trainings: List<TrainingEntity>, bits: List<BitEntity>) {

        val trainingDurations = bits
            .groupBy { it.trainingId }
            .mapValues { (_, bitsList) ->
                bitsList.fold(LocalDateTime.MAX to LocalDateTime.MIN) { duration, bit ->
                    minOf(duration.first, bit.timestamp) to maxOf(duration.second, bit.timestamp)
                }
            }

        trainings.forEach {
            val duration = trainingDurations[it.trainingId]!!
            it.start = duration.first
            it.end = duration.second
        }

        progressNotify.setRange(10, 90)

        val jsonTrainings = trainings.transformToJson(progressNotify)

        val length = jsonTrainings.length().toDouble()

        progressNotify.replaceRange(90, 100)

        jsonTrainings.loopAllIndexed { index ->
            transformNoteToIdx()
            if (index % 25 == 0) {
                progressNotify.update((index / length * 100).toInt())
            }
        }

        progressNotify.removeRange()

        jsonObject.put(DumperDataStructure::trainings.name, jsonTrainings)
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

    private fun List<Any>.transformToJson(progressNotify: RangedProgress): JSONArray {
        val length = size.toDouble()
        return this.mapIndexed { index, it ->
            if (index % 100 == 0) {
                progressNotify.update((index / length * 100).toInt())
            }
            it.jsonify()
        }.toJsonArray
    }

    private fun <T : Any> JSONArray.transformToObject(cls: KClass<T>): List<T> =
        this.map(JSONArray::getJSONObject)
            .map { it.objectify(cls) }

    private fun <T : Any> JSONArray.transformToObject(cls: KClass<T>, progressNotify: RangedProgress): List<T> {
        val length = length().toDouble()
        return this.map(JSONArray::getJSONObject)
            .mapIndexed { index, it ->
                if (index % 100 == 0) {
                    progressNotify.update((index / length * 100).toInt())
                }
                it.objectify(cls)
            }
    }

    private fun JSONArray.loopAll(
        function: JSONObject.() -> Unit
    ) {
        this
            .map(JSONArray::getJSONObject)
            .forEach { it.function() }
    }

    private fun JSONArray.loopAllIndexed(
        function: JSONObject.(Int) -> Unit
    ) {
        this
            .map(JSONArray::getJSONObject)
            .forEachIndexed { index, it -> it.function(index) }
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
