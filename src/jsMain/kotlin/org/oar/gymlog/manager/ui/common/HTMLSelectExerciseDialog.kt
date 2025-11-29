package org.oar.gymlog.manager.ui.common

import org.oar.gymlog.manager.constants.ExportId
import org.oar.gymlog.manager.custom.DefinitionConstants.DIV
import org.oar.gymlog.manager.custom.DefinitionConstants.INPUT
import org.oar.gymlog.manager.custom.DefinitionConstants.SPAN
import org.oar.gymlog.manager.custom.HTMLBlock
import org.oar.gymlog.manager.custom.Utils.createBlock
import org.oar.gymlog.manager.custom.style
import org.oar.gymlog.manager.model.OutputExercise
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.ScrollToOptions

open class HTMLSelectExerciseDialog(
    private val initialExerciseId: Int? = null,
    private val onSelect: (Int?) -> Unit,
): HTMLPopup(id = ID) {

    private val output = read(ExportId.output)!!

    private val filter = createBlock(INPUT, className = "exercise-filter")
    private val list = createBlock(DIV, className = "exercise-list")
    private val muscles = mutableMapOf<Int, MuscleElement>()

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
                            if (output.exercise.containsKey(id)){
                                submit(id)
                            }

                        } else {
                            muscles.values
                                .flatMap { it.exercises }
                                .firstOrNull { !it.block.classList.contains("hide") }
                                ?.also { submit(it.exerciseId) }
                        }
                    }
                    else -> {
                        this@HTMLSelectExerciseDialog.list.element.scrollTo(.0, .0)

                        val value = filter.element.value.trim().lowercase()
                        muscles.values
                            .flatMap { it.exercises }
                            .forEach {
                                it.block.classList.toggle("hide", !match(it.exerciseId, value))
                            }
                        muscles.values.forEach { muscleElements ->
                            val hasVisibleExercise = muscleElements.exercises.any {
                                    val classes = it.block.classList
                                    classes.contains("exercise-item") && !classes.contains("hide")
                                }

                            muscleElements.block.classList.toggle("hide", !hasVisibleExercise)
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
                    +DIV ("exercise-muscle") {
                        muscles[it.muscleId] = MuscleElement(it.muscleId, this)

                        +SPAN("title") {
                            -it.name
                        }
                    }
                }

                output.primaries.forEach { (exerciseId, muscleId) ->
                    val exercise = output.exercise[exerciseId]!!
                    val muscle = muscles[muscleId]!!

                    muscle.block.append(
                        createExerciseListItem(muscle, exercise)
                    )
                }
            }
        }
    }

    override fun render(identifier: Int) {
        when(identifier) {
            -1 -> {
                filter.element.focus()

                if (initialExerciseId != null) {
                    val elem = muscles.values
                        .flatMap { it.exercises }
                        .first { it.exerciseId == initialExerciseId }.block.element

                    list.element.scrollTo(ScrollToOptions(
                        top = elem.offsetTop - list.element.offsetTop - 84.0
                    ))
                }
            }
        }
    }

    private fun match(exerciseId: Int, value: String): Boolean {
        if (value.isBlank()) return true

        value.toIntOrNull()
            ?.also { return exerciseId.toString().contains(value) }

        val exercise = output.exercise[exerciseId]!!
        val exerciseName = exercise.name.lowercase()

        return value
            .split(" ")
            .all { exerciseName.contains(it) }
    }

    private fun createExerciseListItem(muscle: MuscleElement, exercise: OutputExercise): HTMLBlock<HTMLDivElement> =
        createBlock(DIV, className = "exercise-item").apply {
                muscle.exercises.add(ExerciseElement(exercise.exerciseId, this))

                element.apply {
                    onclick = { submit(exercise.exerciseId) }
                    classList.toggle("selected", exercise.exerciseId == initialExerciseId)
                }

                +HTMLExerciseIcon(
                    source = exercise.image,
                    hue = output.muscles[muscle.muscleId]!!.colorHue,
                    size = 48,
                )

                +SPAN("exe-id") {
                    -exercise.exerciseId.toString()
                }
                +SPAN("ex") {
                    -exercise.name
                }
            }

    override fun closePopup() = submit(null)

    private fun submit(id: Int? = null) {
        super.closePopup()
        onSelect(id)
    }

    data class MuscleElement(
        val muscleId: Int,
        val block: HTMLBlock<HTMLDivElement>,
        val exercises: MutableList<ExerciseElement> = mutableListOf(),
    )

    data class ExerciseElement(
        val exerciseId: Int,
        val block: HTMLBlock<HTMLDivElement>,
    )

    companion object {
        const val ID = "exercise-dialog"
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

                    ".exercise-filter" {
                        "align-self" to "center"
                        "width" to "650px"
                        "padding" to "12px"
                        "margin" to "10px 0"
                        "font-size" to "1.2em"
                    }

                    ".exercise-muscle" {
                        ".title" {
                            "display" to "block"
                            "text-align" to "center"
                            "font-weight" to "bold"
                            "font-size" to "1.1em"
                            "padding" to "5px 0"
                            "border-bottom" to "1px dotted gray"
                        }

                        "&.hide" {
                            "display" to "none"
                        }
                    }

                    ".exercise-list" {
                        "overflow" to "auto"
                    }

                    ".exercise-item" {
                        "border" to "dotted gray"
                        "border-width" to "0 0 1px"
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

                        "&.hide" {
                            "display" to "none"
                        }
                    }

                    ".exe-id" {
                        "display" to "inline-block"
                        "text-align" to "right"
                        "width" to "30px"
                    }
                    ".ex::before" {
                        "content" to "\" - \""
                    }
                    ".exe" {
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