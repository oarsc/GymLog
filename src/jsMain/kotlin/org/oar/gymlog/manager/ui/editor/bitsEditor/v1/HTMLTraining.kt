package org.oar.gymlog.manager.ui.editor.bitsEditor.v1

import org.oar.gymlog.manager.lib.HTMLBlock
import org.oar.gymlog.manager.lib.HTMLDefinitionConstants.DIV
import org.oar.gymlog.manager.lib.HTMLDefinitionConstants.H2
import org.oar.gymlog.manager.lib.style
import org.oar.gymlog.manager.model.Output
import org.oar.gymlog.manager.model.OutputBit
import org.oar.gymlog.manager.model.OutputExercise
import org.w3c.dom.HTMLDivElement

class HTMLTraining(
    private val output: Output,
    private val exercise: OutputExercise,
    private val trainingBitGroups: List<List<OutputBit>>,
): HTMLBlock<HTMLDivElement>(DIV, className = CLASS_NAME) {

    private val trainingId: Int

    init {
        val firstTrainingBit = trainingBitGroups.first().first()
        trainingId = firstTrainingBit.trainingId

        +H2 { - "Training #$trainingId" }
        trainingBitGroups.forEach {
            +HTMLEditorExerciseHistory(output, it.toMutableList())
        }
    }

    companion object {
        const val CLASS_NAME = "training"
        init {
            style {
                ".$CLASS_NAME" {
                    "h2" {
                        "margin" to "40px 0 0"
                    }

                    ".training-exercise" {
                        "margin-top" to "25px"
                    }
                }
            }
        }
    }
}