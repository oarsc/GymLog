package org.oar.gymlog.manager.ui.calendar.v1

import org.oar.gymlog.manager.Style.BUTTON_STYLE
import org.oar.gymlog.manager.utils.Export
import org.oar.gymlog.manager.utils.Notifier
import org.oar.gymlog.manager.utils.extensions.DateExt.compareTo
import org.oar.gymlog.manager.utils.extensions.DateExt.toLocaleISODateString
import org.oar.lib.HTMLBlock
import org.oar.lib.HTMLDefinitionConstants.BUTTON
import org.oar.lib.HTMLDefinitionConstants.DIV
import org.oar.lib.HTMLDefinitionConstants.INPUT
import org.oar.lib.HTMLDefinitionConstants.INPUT_NUMBER
import org.oar.lib.style
import org.w3c.dom.HTMLDivElement
import kotlin.js.Date

class HTMLFilter(
    trainingId: Int
): HTMLBlock<HTMLDivElement>(DIV, id = ID) {

    private var output = read(Export.output)!!

    private val dateInput = INPUT("date subtle")
    private val trainingInput = INPUT_NUMBER("training-id")
    private val nextButton = BUTTON(className = BUTTON_STYLE)
    private val prevButton = BUTTON(className = BUTTON_STYLE)

    private var currentTrainingId by renderProperty(
        initial = trainingId,
        identifier = 1
    )

    private val maxTrainingId = output.trainings.maxOf { it.trainingId }

    init {
        prevButton.element.apply {
            textContent = "<"
            onclick = { setTrainingId(currentTrainingId - 1) }
        }

        nextButton.element.apply {
            textContent = ">"
            onclick = { setTrainingId(currentTrainingId + 1) }
        }

        trainingInput.element.apply {
            this.max = maxTrainingId.toString()
            this.min = "1"

            onchange = {
                setTrainingId(value.toIntOrNull())
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
        +trainingInput
        +nextButton
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
        notify(Notifier.trainingIdUpdated, currentTrainingId)
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
                    "text-align" to "center"

                    ".date" {
                        "margin-right" to "20px"
                    }

                    ".training-id" {
                        "text-align" to "center"
                    }
                }
            }
        }
    }
}