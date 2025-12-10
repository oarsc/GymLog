package org.oar.gymlog.manager.ui.calendar.v1

import org.oar.gymlog.manager.custom.DefinitionConstants.DIV
import org.oar.gymlog.manager.custom.DefinitionConstants.SPAN
import org.oar.gymlog.manager.custom.DefinitionConstants.TABLE
import org.oar.gymlog.manager.custom.DefinitionConstants.TBODY
import org.oar.gymlog.manager.custom.DefinitionConstants.TH
import org.oar.gymlog.manager.custom.DefinitionConstants.THEAD
import org.oar.gymlog.manager.custom.DefinitionConstants.TR
import org.oar.gymlog.manager.custom.HTMLBlock
import org.oar.gymlog.manager.custom.style
import org.oar.gymlog.manager.model.Output
import org.oar.gymlog.manager.ui.calendar.model.ExerciseBits
import org.oar.gymlog.manager.ui.support.HTMLExerciseIcon
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLTableCellElement
import org.w3c.dom.HTMLTableElement


class HTMLTrainingExercise(
    private val bitGroup: ExerciseBits,
    private val output: Output,
): HTMLBlock<HTMLDivElement>(DIV, className = CLASS_NAME) {

    private val htmlBits = mutableListOf<HTMLTrainingBit>()

    init {
        +HTMLExerciseIcon(
            source = bitGroup.exercise.image,
            hue = output.primaries
                .first { it.exerciseId == bitGroup.exercise.exerciseId }
                .muscleId.let(output.muscles::get)!!
                .colorHue
        )
        +SPAN("variation-id-label") {
            -bitGroup.variation.variationId.toString()
        }
        +SPAN("exercise-label") {
            -bitGroup.exercise.name
        }
        if (!bitGroup.variation.def) {
            +SPAN("variation-label") {
                -bitGroup.variation.name
            }
        }
        +generateTableBlock()
    }

    private fun generateTableBlock(): HTMLBlock<HTMLTableElement> =
        TABLE {
            +THEAD {
                +TR {
                    +columnTitle("#")
                    +columnTitle("Instant")
                    +columnTitle("SS", "Super Set")
                    +columnTitle("Timestamp")
                    +columnTitle("Reps")
                    +columnTitle("Weight")
                    +columnTitle("Note")
                    +columnTitle("Actions")
                }
            }
            +TBODY("bits") {
                var instants = 0
                bitGroup.bits.forEachIndexed { idx, it ->
                    val index = if (it.instant == true) {
                        instants++
                        -1
                    } else {
                        (idx-instants)+1
                    }

                    +HTMLTrainingBit(it, output).apply {
                        if (index >= 0) {
                            this.index = index
                        }
                        this.onUpdateInstant = { updateIndexes() }
                        htmlBits.add(this)
                    }
                }
            }
        }

    private fun columnTitle(text: String, title: String? = null): HTMLBlock<HTMLTableCellElement> = TH {
        if (title != null) element.title = title
        +SPAN { -text }
    }

    private fun updateIndexes() {
        var instants = 0
        bitGroup.bits.forEachIndexed { idx, it ->
            htmlBits[idx].index = if (it.instant == true) {
                instants++
                -1
            } else {
                (idx-instants)+1
            }
        }
    }

    companion object {
        const val CLASS_NAME = "training-exercise"
        init {
            style {
                ".$CLASS_NAME" {
                    "margin-top" to "50px"
                    "&:first-child" {
                        "margin-top" to "20px"
                    }

                    "th span" {
                        "margin" to "0 5px"
                    }

                    "> *" {
                        "vertical-align" to "middle"
                    }

                    ".variation-id-label" {
                        "font-weight" to "bold"
                        "font-size" to "1.3em"
                        "margin" to "0 6px 0 13px"

                        "&::before" {
                            "content" to "\"[\""
                        }
                        "&::after" {
                            "content" to "\"] \""
                        }
                    }

                    ".variation-label" {
                        "color" to "#999"

                        "&::before" {
                            "content" to "\" - \""
                        }
                    }
                }
            }
        }
    }
}