package org.oar.gymlog.manager.ui.editor.exerciseEditor.v1

import org.oar.gymlog.manager.Style.BUTTON_STYLE
import org.oar.gymlog.manager.constants.NotifierId
import org.oar.gymlog.manager.custom.DefinitionConstants.BUTTON
import org.oar.gymlog.manager.custom.DefinitionConstants.DIV
import org.oar.gymlog.manager.custom.DefinitionConstants.H3
import org.oar.gymlog.manager.custom.DefinitionConstants.TABLE
import org.oar.gymlog.manager.custom.DefinitionConstants.TBODY
import org.oar.gymlog.manager.custom.DefinitionConstants.TD
import org.oar.gymlog.manager.custom.DefinitionConstants.TR
import org.oar.gymlog.manager.custom.HTMLBlock
import org.oar.gymlog.manager.custom.Utils.confirm
import org.oar.gymlog.manager.custom.style
import org.oar.gymlog.manager.model.BarId
import org.oar.gymlog.manager.model.ExerciseType
import org.oar.gymlog.manager.model.GymRelation
import org.oar.gymlog.manager.model.GymRelation.STRICT_RELATION
import org.oar.gymlog.manager.model.Output
import org.oar.gymlog.manager.model.OutputExercise
import org.oar.gymlog.manager.model.OutputVariation
import org.oar.gymlog.manager.model.Step
import org.oar.gymlog.manager.model.WeightSpec
import org.oar.gymlog.manager.ui.support.HTMLInputEditBind
import org.oar.gymlog.manager.ui.support.HTMLSelectEditBind
import org.w3c.dom.HTMLDivElement

class HTMLVariationEditor(
    private val output: Output,
    variationId: Int
): HTMLBlock<HTMLDivElement>(DIV, className = CLASS_NAME) {

    private val exercise: OutputExercise
    private val variation: OutputVariation

    init {
        output.variation[variationId]!!.also {
            exercise = it.first
            variation = it.second
        }

        val gymsBlock = HTMLSelectEditBind(
            obj = variation,
            accessor = OutputVariation::gymId,
            options = output.gyms
                .mapIndexed { idx, it -> (idx + 1).toString() to it }
                .toMap(),
            mapperBack = { if (isEmpty()) null else toInt() },
            mapper = { this?.toString() ?: "" }
        ).apply {
            element.apply {
                disabled = variation.gymRelation != STRICT_RELATION
                classList.toggle("subtle", disabled)
            }
        }

        if (!variation.def) {
            +H3 { -"Variation #${variation.variationId}" }
            +HTMLInputEditBind(
                obj = variation,
                accessor = OutputVariation::name,
                mapperBack = { it.trim() }
            ).apply { classList.add("name","subtle") }
        } else {
            +H3 { -"Default variation #${variation.variationId}" }
        }

        +TABLE {
            +TBODY {
                +TR {
                    +TD { -"Exercise Type" }
                    +TD {
                        +HTMLSelectEditBind(
                            obj = variation,
                            accessor = OutputVariation::type,
                            options = ExerciseType.entries
                                .mapIndexed { idx, it -> idx.toString() to it.name }
                                .toMap(),
                            mapperBack = { ExerciseType.entries[toInt()] },
                            mapper = { ordinal.toString() }
                        )
                    }

                    +TD { -"Gym Relation" }
                    +TD {
                        +HTMLSelectEditBind(
                            obj = variation,
                            accessor = OutputVariation::gymRelation,
                            options = GymRelation.entries
                                .mapIndexed { idx, it -> idx.toString() to it.name }
                                .toMap(),
                            mapperBack = { GymRelation.entries[toInt()] },
                            mapper = { ordinal.toString() }
                        ).apply {
                            onchange = {
                                gymsBlock.element.apply {
                                    disabled = it != STRICT_RELATION
                                    classList.toggle("subtle", disabled)
                                    if (disabled) {
                                        value = ""
                                        variation.gymId = null
                                    } else {
                                        value = "1"
                                        variation.gymId = 1
                                    }
                                }
                            }
                        }
                    }
                }

                +TR {
                    +TD { -"Last bar" }
                    +TD {
                        +HTMLSelectEditBind(
                            obj = variation,
                            accessor = OutputVariation::lastBarId,
                            options = BarId.entries
                                .mapIndexed { idx, it -> idx.toString() to it.toString() }
                                .toMap(),
                            mapperBack = { BarId.entries[toInt()] },
                            mapper = { this?.ordinal?.toString() ?: "0" }
                        )
                    }

                    +TD { -"Gym Id" }
                    +TD { +gymsBlock }
                }

                +TR {
                    +TD { -"Last Weight Spec" }
                    +TD {
                        +HTMLSelectEditBind(
                            obj = variation,
                            accessor = OutputVariation::lastWeightSpec,
                            options = WeightSpec.entries
                                .mapIndexed { idx, it -> idx.toString() to it.name }
                                .toMap(),
                            mapperBack = { WeightSpec.entries[toInt()] },
                            mapper = { ordinal.toString() }
                        )
                    }

                    +TD { -"Last Rest Time" }
                    +TD {
                        +HTMLInputEditBind(
                            obj = variation,
                            accessor = OutputVariation::lastRestTime,
                            mapper = { if (this < 0) "" else toString() },
                            mapperBack = { it.takeIf(String::isNotBlank)?.toIntOrNull() ?: -1 }
                        ).apply {
                            element.apply {
                                type = "number"
                                min = "0"
                            }
                        }
                    }
                }

                +TR {
                    +TD { -"Last Step" }
                    +TD {
                        +HTMLSelectEditBind(
                            obj = variation,
                            accessor = OutputVariation::lastStep,
                            options = Step.entries
                                .mapIndexed { idx, it -> idx.toString() to it.toString() }
                                .toMap(),
                            mapperBack = { Step.entries[toInt()] },
                            mapper = { this.ordinal.toString() }
                        )
                    }
                }
            }
        }

        +DIV("actions") {
            if (!variation.def) {
                +BUTTON("$BUTTON_STYLE big transparent") {
                    element.onclick = {
                        if (confirm("Are you sure?")) {
                            output.variations.remove(variation)
                            output.variation.remove(variation.variationId)

                            val defVariationId = output.variations.first { it.exerciseId == exercise.exerciseId && it.def }.variationId
                            output.bits.forEach {
                                if (it.variationId == variation.variationId) {
                                    it.variationId = defVariationId
                                }
                            }
                            notify(NotifierId.reload)
                        }
                    }
                    -"×"
                }
            }

            +BUTTON("$BUTTON_STYLE big green") {
                element.onclick = {
                    val newVariationId = output.variations.maxOf { it.variationId } + 1
                    val newVariation = variation.copy(
                        def = false,
                        variationId = newVariationId,
                        name = "Variation #$newVariationId"
                    )
                    output.variations.add(newVariation)
                    output.variation[newVariationId] = exercise to newVariation
                    notify(NotifierId.reload)
                }
//                -"+↓"
                -"Clone"
            }

            +BUTTON("$BUTTON_STYLE big") {
                element.onclick = {
                    notify(NotifierId.editorBitsPanel, variation)
                }
//                -"↓↓"
                -"Find registries"
            }
        }
    }

    companion object {
        const val CLASS_NAME = "variation-editor"
        init {
            style {
                ".$CLASS_NAME" {
                    "&:not(:first-child)" {
                        "margin-top" to "30px"
                    }

                    "table" {
                        "td" {
                            "padding-right" to "20px"
                            "input, select" {
                                "width" to "100%"
                            }
                        }
                    }
                    ".actions" {
                        "text-align" to "right"
                        "margin-right" to "20px"
                    }

                    "h3" {
                        "display" to "inline-block"
                    }

                    ".name" {
                        "padding" to "10px"
                        "width" to "500px"
                        "margin-left" to "10px"
                        "font-size" to "16px"
                    }
                }
            }
        }
    }
}