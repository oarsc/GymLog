package org.oar.gymlog.manager.ui.editor.bitsEditor.v1

import org.oar.gymlog.manager.constants.ExportId
import org.oar.gymlog.manager.custom.DefinitionConstants.DIV
import org.oar.gymlog.manager.custom.HTMLBlock
import org.oar.gymlog.manager.custom.style
import org.oar.gymlog.manager.model.OutputVariation
import org.w3c.dom.HTMLDivElement

class HTMLTrainingList(
    private val variation: OutputVariation
): HTMLBlock<HTMLDivElement>(DIV, id = ID) {

    private val output = read(ExportId.output)!!
    private val trainings = output.bits
        .asSequence()
        .filter { it.variationId == variation.variationId }
        .groupBy { it.trainingId }
        .entries.reversed()
        .chunked(MAX_ELEMENTS_PER_PAGE)
        .map { page -> page.associate { it.key to it.value } }
    private var page = 0

    private fun createList() = DIV {
        trainings[page].forEach { (trainingId, bits) ->
            +HTMLTraining(output, variation, trainingId, bits)
        }
    }

    override fun render(identifier: Int) {
        when(identifier) {
            -1 -> +createList()
        }
    }

    companion object {
        private const val MAX_ELEMENTS_PER_PAGE = 10
        const val ID = "training-list"
        init {
            style {
                "#$ID" {
                }
            }
        }
    }
}