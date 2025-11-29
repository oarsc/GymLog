package org.oar.gymlog.manager.process

import kotlinx.serialization.json.Json
import org.oar.gymlog.manager.model.BarId
import org.oar.gymlog.manager.model.Output
import org.oar.gymlog.manager.utils.DateExt.compareTo
import kotlin.js.Date

object Export {
    fun export(output: Output): Output = output.apply {
        sort()
        removeDefaults()
        updateTrainingsTimestamps()
        notes()

        val outputString = Json.encodeToString(Output.serializer(), output)
        Download.download("$outputString\n")
    }

    private fun Output.sort() {
        bits.sortBy { it.timestamp.getTime() }
        exercises.sortBy { it.exerciseId }
        trainings.sortBy { it.trainingId }
        primaries.sortWith(compareBy(
            { it.muscleId },
            { it.exerciseId }
        ))
        secondaries.sortWith(compareBy(
            { it.muscleId },
            { it.exerciseId }
        ))
        variationsSort()
    }

    private fun Output.variationsSort() {
        val variationsIdMap = variations // old -> new
            .filter { it.def }
            .associate {
                val oldVariationId = it.variationId
                it.variationId = it.exerciseId
                oldVariationId to it.exerciseId
            }
            .toMutableMap()
        var lastId = variationsIdMap.values.maxOf { it }

        exercises
            .flatMap { ex -> variations.filter { !it.def && it.exerciseId == ex.exerciseId } }
            .forEach {
                variationsIdMap[it.variationId] = ++lastId
                it.variationId = lastId
            }

        variations.sortWith(compareBy(
            { it.exerciseId },
            { it.variationId }
        ))

        bits.forEach {
            it.variationId = variationsIdMap[it.variationId]!!
        }

        variation = variations
            .associateBy { it.variationId }
            .mapValues { Pair(exercise[it.value.exerciseId]!!, it.value) }
            .toMutableMap()
    }

    private fun Output.removeDefaults() {
        bits.forEach {
            if (it.instant == false) it.instant = null
            if (it.kilos == true) it.kilos = null
            if (it.superSet == 0) it.superSet = null
        }

        variations.forEach {
            if (it.lastBarId == BarId.NONE) it.lastBarId = null
            if (it.gymId == 0) it.gymId = null
        }
    }

    private fun Output.updateTrainingsTimestamps() {
        trainings.forEach { training ->
            val (minDate, maxDate) = bits
                .filter { it.trainingId == training.trainingId }
                .map { it.timestamp }
                .fold(Pair(Date(), Date(0L))) { (min, max), timestamp ->
                    Pair(
                        if (timestamp < min) timestamp else min,
                        if (timestamp > max) timestamp else max
                    )
                }

            if (minDate < maxDate) {
                training.start = minDate
                training.end = maxDate
            }
        }
    }

    private fun Output.notes() {
        val trimmed = notes.map { it.trim() }
        val newNotes = (bits.mapNotNull { it.note } +  trainings.mapNotNull { it.note }).toSet()
            .map(trimmed::get)
            .distinct()
            .sorted()

        bits
            .filter { it.note != null }
            .forEach {
                it.note = newNotes.indexOf(trimmed[it.note!!])
            }

        trainings
            .filter { it.note != null }
            .forEach {
                it.note = newNotes.indexOf(trimmed[it.note!!])
            }

        notes = newNotes.toMutableList()
        note = newNotes
            .mapIndexed { index, note -> index to note }
            .toMap()
            .toMutableMap()
    }
}