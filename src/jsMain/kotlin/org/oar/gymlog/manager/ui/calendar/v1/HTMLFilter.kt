package org.oar.gymlog.manager.ui.calendar.v1

import org.oar.gymlog.manager.Style.BUTTON_STYLE
import org.oar.gymlog.manager.constants.ExportId
import org.oar.gymlog.manager.constants.NotifierId
import org.oar.gymlog.manager.custom.DefinitionConstants.BUTTON
import org.oar.gymlog.manager.custom.DefinitionConstants.DIV
import org.oar.gymlog.manager.custom.DefinitionConstants.INPUT
import org.oar.gymlog.manager.custom.HTMLBlock
import org.oar.gymlog.manager.custom.style
import org.oar.gymlog.manager.utils.DateExt.compareTo
import org.oar.gymlog.manager.utils.DateExt.toLocaleISODateString
import org.w3c.dom.HTMLDivElement
import kotlin.js.Date

class HTMLFilter(
    trainingId: Int
): HTMLBlock<HTMLDivElement>(DIV, id = ID) {

    private var output = read(ExportId.output)!!

    private val dateInput = INPUT()
    private val trainingInput = INPUT()
    private val nextButton = BUTTON(className = BUTTON_STYLE)
    private val prevButton = BUTTON(className = BUTTON_STYLE)

    private var currentTrainingId by renderProperty(
        initial = trainingId,
        identifier = 1
    )

    private val maxTrainingId = output.trainings.maxOf { it.trainingId }

    init {
        nextButton.element.apply {
            textContent = ">"
            onclick = { setTrainingId(currentTrainingId + 1) }
        }

        prevButton.element.apply {
            textContent = "<"
            onclick = { setTrainingId(currentTrainingId - 1) }
        }

        trainingInput.element.apply {
            type = "number"
            this.max = maxTrainingId.toString()
            this.min = "1"

            onblur = {
                setTrainingId(value.toIntOrNull())
            }

            onkeyup = {
                if (it.keyCode == 13 || it.keyCode == 27) { // Enter / Escape
                    blur()
                }
            }
        }

        dateInput.element.apply {
            type = "date"
            this.max = output.training[maxTrainingId]!!.start.toLocaleISODateString()
            this.min = output.trainings.first().start.toLocaleISODateString()

            onchange = {
                val date = Date(value)
                setTrainingId(output.trainings.firstOrNull { it.start > date }?.trainingId)
            }
        }

        updateInputs()

        +dateInput
        +prevButton
        +nextButton
        +trainingInput
    }

    override fun render(identifier: Int) {
        when (identifier) {
            -1, 1 -> {
                nextButton.element.disabled = currentTrainingId >= maxTrainingId
                prevButton.element.disabled = currentTrainingId <= 1
            }
        }
    }

    private fun setTrainingId(trainingId: Int?) {
        currentTrainingId = trainingId?.coerceIn(1, maxTrainingId) ?: maxTrainingId
        notify(NotifierId.trainingIdUpdated, currentTrainingId)
        updateInputs()
    }

    private fun updateInputs() {
        trainingInput.element.value = currentTrainingId.toString()
        dateInput.element.value = output
            .training[currentTrainingId]!!
            .start
            .toLocaleISODateString()
    }

    companion object {
        private const val DAY = 86400000L
        const val ID = "calendar-filter"
        init {
            style {
                "#$ID" {
                }
            }
        }
    }
}