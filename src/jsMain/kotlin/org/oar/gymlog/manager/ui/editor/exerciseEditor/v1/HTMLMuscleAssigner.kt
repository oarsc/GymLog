package org.oar.gymlog.manager.ui.editor.exerciseEditor.v1

import org.oar.gymlog.manager.Style.BUTTON_STYLE
import org.oar.gymlog.manager.custom.DefinitionConstants.BUTTON
import org.oar.gymlog.manager.custom.DefinitionConstants.DIV
import org.oar.gymlog.manager.custom.DefinitionConstants.OPTION
import org.oar.gymlog.manager.custom.DefinitionConstants.SELECT
import org.oar.gymlog.manager.custom.HTMLBlock
import org.oar.gymlog.manager.custom.style
import org.oar.gymlog.manager.model.Output
import org.oar.gymlog.manager.model.OutputExercise
import org.oar.gymlog.manager.model.OutputMuscleRelation
import org.w3c.dom.HTMLDivElement

class HTMLMuscleAssigner(
    private val output: Output,
    private val exercise: OutputExercise,
    private val musclesRelation: MutableList<OutputMuscleRelation>,
    private val allowEmpty: Boolean = false
): HTMLBlock<HTMLDivElement>(DIV, className = CLASS_NAME) {

    private val muscles get() =
        musclesRelation.filter { it.exerciseId == exercise.exerciseId }.map { it.muscleId }

    init {
        val muscleList = DIV {
            muscles.forEach { muscleId ->
                +muscleRow(muscleId)
            }
        }

        +muscleList
        +BUTTON("$BUTTON_STYLE transparent") {
            element.onclick = {
                val muscleIds = muscles
                val newMuscleId = output.muscles.keys.firstOrNull { !muscleIds.contains(it) }
                if (newMuscleId != null) {
                    muscleList.append(muscleRow(newMuscleId))
                    addMuscleRelation(newMuscleId)
                }
            }
            -"+"
        }
    }

    private fun muscleRow(muscleId: Int) = DIV {
        val select = createMuscleSelect(muscleId)
        +BUTTON("$BUTTON_STYLE transparent") {
            -"-"
            element.onclick = {
                val selectedMuscleId = select.element.value.toInt()
                if (removeMuscleRelation(selectedMuscleId)) {
                    this@DIV.remove()
                }
            }
        }
        +select
    }

    private fun createMuscleSelect(muscleId: Int) = SELECT {
        var prevMuscleId = muscleId
        element.apply {
            fun updateColor(muscleId: Int) {
                val muscleColor = output.muscles[muscleId]!!.color
                style.boxShadow = "$muscleColor 2px 2px 0px 0px"
                style.borderColor = muscleColor
            }

            className = "cstm-muscle-select"
            value = muscleId.toString()
            updateColor(muscleId)

            onchange = {
                val newValue = value.toInt()
                if (canChange(newValue)) {
                    updateMuscleRelation(prevMuscleId, newValue)
                    updateColor(newValue)
                    prevMuscleId = newValue
                } else {
                    value = prevMuscleId.toString()
                }
            }
        }

        output.muscles.values.forEach {
            +OPTION {
                element.apply {
                    value = it.muscleId.toString()
                    text = it.name
                    selected = it.muscleId == muscleId
                }
            }
        }
    }

    private fun canChange(muscleId: Int): Boolean = !muscles.contains(muscleId)

    private fun updateMuscleRelation(prevMuscleId: Int, muscleId: Int) {
        musclesRelation.first { it.exerciseId == exercise.exerciseId && it.muscleId == prevMuscleId }
            .muscleId = muscleId
    }

    private fun removeMuscleRelation(muscleId: Int): Boolean {
        if (allowEmpty || muscles.size > 1) {
            musclesRelation.removeAll { it.exerciseId == exercise.exerciseId && it.muscleId == muscleId }
            return true
        }
        return false
    }

    private fun addMuscleRelation(muscleId: Int) {
        musclesRelation.add(OutputMuscleRelation(exercise.exerciseId, muscleId))
    }

    companion object {
        const val CLASS_NAME = "muscle-selector"
        init {
            style {
                ".$CLASS_NAME" {
                    "select" {
                        "border" to "1px solid #444"
                        "border-radius" to "3px"
                        "padding" to "6px"
                        "margin" to "4px 0"
                    }

                    "button" {
                        "height" to "33px"
                        "width" to "33px"
                        "font-size" to "1em"
                    }
                }
            }
        }
    }
}