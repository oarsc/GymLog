package org.oar.gymlog.manager.ui.editor.bitsEditor.v1

import org.oar.gymlog.manager.custom.DefinitionConstants.DIV
import org.oar.gymlog.manager.custom.HTMLBlock
import org.oar.gymlog.manager.custom.style
import org.oar.gymlog.manager.model.Output
import org.oar.gymlog.manager.model.OutputBit
import org.oar.gymlog.manager.model.OutputVariation
import org.w3c.dom.HTMLDivElement

class HTMLTraining(
    private val output: Output,
    private val variation: OutputVariation,
    private val trainingId: Int,
    private val bits: List<OutputBit>,
): HTMLBlock<HTMLDivElement>(DIV, className = CLASS_NAME) {

    init {
        +trainingId.toString()
        bits.forEach {
            +DIV {
                +it.totalWeight.toString()
            }
        }
        +"------------"
    }

    companion object {
        const val CLASS_NAME = "training"
        init {
            style {
                "#$CLASS_NAME" {
                }
            }
        }
    }
}