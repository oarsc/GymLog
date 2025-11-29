package org.oar.gymlog.manager.ui.common

import org.oar.gymlog.manager.constants.ExportId
import org.oar.gymlog.manager.custom.DefinitionConstants.DIV
import org.oar.gymlog.manager.custom.DefinitionConstants.INPUT
import org.oar.gymlog.manager.custom.DefinitionConstants.SPAN
import org.oar.gymlog.manager.custom.HTMLBlock
import org.oar.gymlog.manager.custom.Utils.createBlock
import org.oar.gymlog.manager.custom.style
import org.oar.gymlog.manager.model.OutputExercise
import org.oar.gymlog.manager.model.OutputMuscleRelation
import org.oar.gymlog.manager.model.OutputVariation
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.ScrollToOptions

open class HTMLSelectVariationDialog(
    private val initialVariationId: Int? = null,
    private val onSelect: (Int?) -> Unit,
): HTMLPopup(id = ID) {

    private val output = read(ExportId.output)!!

    private val filter = createBlock(INPUT, className = "variation-filter")
    private val list = createBlock(DIV, className = "variation-list")
    private val muscles = mutableMapOf<Int, MuscleElementWrapper>()

    init {
        filter.element.apply {
            onkeyup = { event ->
                when (event.keyCode) {
                    // Escape
                    27 -> submit()
                    // Enter
                    13 -> {
                        val id = filter.element.value.toIntOrNull()
                        if (id != null) {
                            if (output.variation.containsKey(id)) {
                                submit(id)
                            }

                        } else {
                            muscles.values
                                .flatMap { it.exercises }
                                .firstOrNull { !it.block.classList.contains("hide") }
                                ?.also { exerciseElement ->
                                    val variationsId = findMatch(exerciseElement, filter.element.value.trim().lowercase())
                                        .takeIf { it >= 0 }

                                    submit(variationsId)
                                }
                        }
                    }
                    else -> {
                        this@HTMLSelectVariationDialog.list.element.scrollTo(.0, .0)

                        val value = filter.element.value.trim().lowercase()
                        muscles.values
                            .flatMap { it.exercises }
                            .forEach {
                                it.block.classList.toggle("hide", !match(it, value))
                            }

                        muscles.values.forEach { muscleElements ->
                            val hasVisibleExercises = muscleElements.exercises
                                .map { it.block.classList }
                                .any { !it.contains("hide") }

                            muscleElements.block.classList.toggle("hide", !hasVisibleExercises)
                        }
                    }
                }
                Unit
            }
        }

        content.apply {
            +filter
            +list.apply {
                output.muscles.values.forEach {
                    +DIV("variation-muscle") {
                        muscles[it.muscleId] = MuscleElementWrapper(it.muscleId, this)
                        +SPAN("title") {
                            -it.name
                        }
                    }
                }

                output.muscles.values.forEach { muscle ->
                    val muscleWrapper = muscles[muscle.muscleId]!!

                    output.primaries
                        .filter { it.muscleId == muscle.muscleId }
                        .map(OutputMuscleRelation::exerciseId)
                        .mapNotNull(output.exercise::get)
                        .forEach { exercise ->
                            val (defaultVariation, variations) = output.variations
                                .filter { it.exerciseId == exercise.exerciseId }
                                .partition { it.def }
                                .let { it.first.single() to it.second}

                            muscleWrapper.block.append(
                                createVariationListItem(muscleWrapper, exercise, defaultVariation, variations)
                            )
                        }
                }
            }
        }
    }

    override fun render(identifier: Int) {
        when(identifier) {
            -1 -> {
                filter.element.focus()

                if (initialVariationId != null) {
                    val exerciseId = output.variation[initialVariationId]!!.first.exerciseId

                    val elem = muscles.values
                        .flatMap { it.exercises }
                        .first { it.exercise.exerciseId == exerciseId }.block.element

                    list.element.scrollTo(ScrollToOptions(
                        top = elem.offsetTop - list.element.offsetTop - 84.0
                    ))
                }
            }
        }
    }

    private fun match(exerciseElement: ExerciseElement, value: String): Boolean {
        if (value.isBlank()) return true

        value.toIntOrNull()
            ?.also {
                val ids = exerciseElement.variations.map { it.variationId } + exerciseElement.exercise.exerciseId
                return ids.any { it.toString().contains(value) }
            }

        val name = exerciseElement.variations
            .map { it.name.lowercase() }
            .let { it + exerciseElement.exercise.name.lowercase() }
            .joinToString(" ")

        return value
            .split(" ")
            .all { name.contains(it) }
    }

    private fun findMatch(exerciseElement: ExerciseElement, value: String): Int {
        val splits = value.split(" ")

        val exerciseName = exerciseElement.exercise.name.lowercase()

        if (splits.all { exerciseName.contains(it) }) {
            return exerciseElement.defaultVariation.variationId
        }

        exerciseElement.variations.forEach { variation ->
            val variableName = "$exerciseName ${variation.name.lowercase()}"

            if (splits.all { variableName.contains(it) }) {
                return variation.variationId
            }
        }
        return -1
    }


    private fun createVariationListItem(
        muscleWrapper: MuscleElementWrapper,
        exercise: OutputExercise,
        defaultVariation: OutputVariation,
        variations: List<OutputVariation>
    ): HTMLBlock<HTMLDivElement> = createBlock(DIV, className = "exercise-item").apply {
        muscleWrapper.exercises.add(ExerciseElement(exercise, defaultVariation, variations, this))

        +DIV("default-variation-item") {
            element.apply {
                onclick = { submit(defaultVariation.variationId) }
                classList.toggle("selected", defaultVariation.variationId == initialVariationId)
            }

            +HTMLExerciseIcon(
                source = exercise.image,
                hue = output.muscles[muscleWrapper.muscleId]!!.colorHue,
                size = 48,
            )

            +SPAN("var-id") {
                -defaultVariation.variationId.toString()
            }

            +SPAN("ex") {
                -exercise.name
            }
        }

        variations.forEach { variation ->
            +DIV("variation-item") {
                element.apply {
                    onclick = { submit(variation.variationId) }
                    classList.toggle("selected", variation.variationId == initialVariationId)
                }

                +SPAN("var-id") {
                    -variation.variationId.toString()
                }
                +SPAN("var") {
                    -variation.name
                }
            }
        }
    }

    override fun closePopup() = submit(null)

    private fun submit(id: Int? = null) {
        super.closePopup()
        onSelect(id)
    }

    data class MuscleElementWrapper(
        val muscleId: Int,
        val block: HTMLBlock<HTMLDivElement>,
        val exercises: MutableList<ExerciseElement> = mutableListOf(),
    )

    data class ExerciseElement(
        val exercise: OutputExercise,
        val defaultVariation: OutputVariation,
        val variations: List<OutputVariation>,
        val block: HTMLBlock<HTMLDivElement>,
    )

    companion object {
        const val ID = "variation-dialog"
        init {
            style {
                "#$ID .content" {
                    "display" to "flex"
                    "flex-direction" to "column"
                    "width" to "750px"
                    "height" to "min(100vh, 900px)"
                    "min-height" to "200px"
                    "margin" to "auto"
                    "background-color" to "white"

                    ".variation-filter" {
                        "align-self" to "center"
                        "width" to "650px"
                        "padding" to "12px"
                        "margin" to "10px 0"
                        "font-size" to "1.2em"
                    }

                    ".variation-muscle" {
                        ".title" {
                            "&:not(:first-child)" {
                                "border-top" to "1px dotted gray"
                            }

                            "display" to "block"
                            "text-align" to "center"
                            "font-weight" to "bold"
                            "font-size" to "1.1em"
                            "padding" to "5px 0"
                        }

                        "&.hide" {
                            "display" to "none"
                        }
                    }

                    ".variation-list" {
                        "overflow" to "auto"
                    }

                    ".exercise-item.hide" {
                        "display" to "none"
                    }

                    ".default-variation-item" {
                        "border" to "dotted gray"
                        "border-width" to "1px 0 0"
                    }

                    ".variation-item, .default-variation-item" {
                        "padding" to "2px 10px"
                        "user-select" to "none"
                        "cursor" to "pointer"
                        "transition" to "background-color 0.3s"

                        "&:hover" {
                            "background-color" to "#EEE"
                        }

                        "&.selected" {
                            "background-color" to "#a1c6ff"
                        }
                    }

                    ".var-id" {
                        "display" to "inline-block"
                        "text-align" to "right"
                        "width" to "30px"
                    }
                    ".ex::before" {
                        "content" to "\" - \""
                    }
                    ".var" {
                        "color" to "gray"
                        "&::before" {
                            "content" to "\" - \""
                        }
                    }
                }
            }
        }
    }
}