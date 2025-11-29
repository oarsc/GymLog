package org.oar.gymlog.manager.ui.input

import kotlinx.serialization.json.Json
import org.oar.gymlog.manager.constants.ExportId
import org.oar.gymlog.manager.constants.NotifierId
import org.oar.gymlog.manager.custom.DefinitionConstants.INPUT
import org.oar.gymlog.manager.custom.HTMLBlock
import org.oar.gymlog.manager.custom.style
import org.oar.gymlog.manager.model.Muscle
import org.oar.gymlog.manager.model.Output
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLParagraphElement
import org.w3c.files.FileReader
import org.w3c.files.get

class HTMLInputFileReader : HTMLBlock<HTMLInputElement>(INPUT, id = ID) {

    private var output: Output? = null

    init {
        expose(ExportId.output) { output }

        element.apply {
            type = "file"
            accept = ".json"

            addEventListener("change", {
                val loadingElement = findById<HTMLParagraphElement>(HTMLInputLoading.ID)!!.element
                val file = files?.get(0)

                if (file != null) {
                    loadingElement.style.display = "block"

                    FileReader().apply {
                        onload = {
                            val content = result as String
                            output = Json.decodeFromString(Output.serializer(), content)
                            addIndexes(output!!)
                            loadingElement.style.display = "none"
                            notify(NotifierId.fileLoaded)
                        }
                        readAsText(file)
                    }
                }
            })
        }
    }

    private fun addIndexes(output: Output) {
        output.note = output.notes
            .mapIndexed { index, note -> index to note }
            .toMap()
            .toMutableMap()

        output.training = output.trainings.associateBy { it.trainingId }.toMutableMap()
        output.exercise = output.exercises.associateBy { it.exerciseId }.toMutableMap()
        output.variation = output.variations
            .associateBy { it.variationId }
            .mapValues { Pair(output.exercise[it.value.exerciseId]!!, it.value) }
            .toMutableMap()

        output.muscles = mutableMapOf(
            1 to Muscle(1, "Chest", 213, "#9675CE"),
            2 to Muscle(2, "Back", 188, "#1073AE"),
            3 to Muscle(3, "Lower Back", 155, "#4DD0E2"),
            4 to Muscle(4, "Shoulders", 316, "#E05E55"),
            5 to Muscle(5, "Trapezius", 290, "#F05F92"),
            6 to Muscle(6, "Biceps", 81, "#8DCA1A"),
            7 to Muscle(7, "Triceps", 124, "#01BFA5"),
            8 to Muscle(8, "Forearm", 100, "#71BA6E"),
            9 to Muscle(9, "Quadriceps", 27, "#FFD33F"),
            10 to Muscle(10, "Hamstrings", 11, "#F9A825"),
            11 to Muscle(11, "Glutes", 340, "#F96825"),
            12 to Muscle(12, "Calves", 20, "#B7AB6E"),
            13 to Muscle(13, "Abdominals", 229, "#CA62F0"),
            14 to Muscle(14, "Cardio", 100, "#AAABAA"),
        )
    }

    companion object {
        const val ID = "file-input"
        init {
            style {
                "#$ID" {
                }
            }
        }
    }
}