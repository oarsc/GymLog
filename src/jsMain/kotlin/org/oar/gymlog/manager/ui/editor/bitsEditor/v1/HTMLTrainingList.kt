package org.oar.gymlog.manager.ui.editor.bitsEditor.v1

import org.oar.gymlog.manager.Style.BUTTON_STYLE
import org.oar.gymlog.manager.model.OutputBit
import org.oar.gymlog.manager.model.OutputExercise
import org.oar.gymlog.manager.utils.Export
import org.oar.lib.HTMLBlock
import org.oar.lib.HTMLDefinitionConstants.BUTTON
import org.oar.lib.HTMLDefinitionConstants.DIV
import org.oar.lib.HTMLDefinitionConstants.INPUT
import org.oar.lib.HTMLDefinitionConstants.SPAN
import org.oar.lib.style
import org.w3c.dom.HTMLDivElement

class HTMLTrainingList(
    private val exercise: OutputExercise
): HTMLBlock<HTMLDivElement>(DIV, id = ID) {
    private val output = read(Export.output)!!
    private val variations = output.variations.filter { it.exerciseId == exercise.exerciseId }.sortedBy { !it.def }
    private val trainingsPages: List<List<List<IndexedValue<OutputBit>>>>
    private var page = 0

    private val pager = INPUT("pager subtle")
    private val nextButton = BUTTON(className = BUTTON_STYLE)
    private val prevButton = BUTTON(className = BUTTON_STYLE)
    private val listBlock: HTMLBlock<HTMLDivElement> = DIV()

    init {
        val variationIds = variations.map { it.variationId }
        trainingsPages = output.bits
            .asSequence()
            .withIndex()
            .filter { it.value.variationId in variationIds }
            .groupBy { it.value.trainingId }
            .values
            .reversed()
            .chunked(MAX_ELEMENTS_PER_PAGE)

        prevButton.element.apply {
            disabled = true
            textContent = "<"
            onclick = { setPage(page - 1) }
        }
        nextButton.element.apply {
            textContent = ">"
            onclick = { setPage(page + 1) }
        }
        pager.element.apply {
            value = (page+1).toString()
            min = "1"
            max = (trainingsPages.size).toString()
            onchange = { setPage(value.toIntOrNull()?.minus(1) ?: 0) }
        }
        updateList()

        +DIV(id = "pager-container") {
            +prevButton
            +pager
            +SPAN {
                -"/${trainingsPages.size}"
            }
            +nextButton
        }
        +listBlock
    }

    private fun setPage(newPage: Int) {
        page = newPage.coerceIn(0, trainingsPages.size - 1)
        pager.element.value = (page + 1).toString()
        prevButton.element.disabled = page == 0
        nextButton.element.disabled = page == trainingsPages.size - 1
        updateList()
    }

    private fun updateList() {
        listBlock.apply {
            clear()

            trainingsPages[page].forEach { training ->
                +HTMLTraining(output, exercise, training.splitByIndexAndVariation())
            }
        }
    }

    private fun List<IndexedValue<OutputBit>>.splitByIndexAndVariation(): List<List<OutputBit>> {
        var nextIndex = first().index
        var lastVariationId = first().value.variationId

        var bits = mutableListOf<OutputBit>()
        val result = mutableListOf<List<OutputBit>>(bits)

        forEach { (idx, it) ->
            if (idx == nextIndex && it.variationId == lastVariationId) {
                bits.add(it)
                nextIndex++
            } else {
                nextIndex = idx+1
                lastVariationId = it.variationId
                bits = mutableListOf(it)
                result.add(bits)
            }
        }

        return result
    }

    companion object {
        private const val MAX_ELEMENTS_PER_PAGE = 10
        const val ID = "training-list"
        init {
            style {
                "#$ID" {
                    ".pager" {
                        "text-align" to "right"
                        "width" to "20px"
                        "font-size" to "1em"
                    }
                }

                "#pager-container" {
                    "text-align" to "center"
                }
            }
        }
    }
}