package org.oar.gymlog.manager.ui.editor

import org.oar.gymlog.manager.Style.BUTTON_STYLE
import org.oar.gymlog.manager.constants.ExportId
import org.oar.gymlog.manager.constants.NotifierId
import org.oar.gymlog.manager.custom.DefinitionConstants.BUTTON
import org.oar.gymlog.manager.custom.DefinitionConstants.DIV
import org.oar.gymlog.manager.custom.HTMLBlock
import org.oar.gymlog.manager.custom.style
import org.oar.gymlog.manager.model.Output
import org.oar.gymlog.manager.model.OutputExercise
import org.oar.gymlog.manager.ui.common.HTMLSelectExerciseDialog
import org.w3c.dom.HTMLDivElement

class HTMLSelector: HTMLBlock<HTMLDivElement>(DIV, id = ID) {

    private var output: Output = read(ExportId.output)!!
    private var exerciseSelected: OutputExercise? = null

    init {
        +BUTTON("$BUTTON_STYLE big") {
            element.onclick = { showDialog() }
            -"Select exercise"
        }
    }

    private fun showDialog() {
        +HTMLSelectExerciseDialog(initialExerciseId = exerciseSelected?.exerciseId) {
            if (it != null) {
                val exercise = output.exercise[it]!!
                exerciseSelected = exercise
                notify(NotifierId.exerciseSelected, exercise)
            }
        }
    }

    companion object {
        const val ID = "selector"
        init {
            style {
                "#$ID" {
                    "position" to "absolute"
                    "right" to "0"
                    "top" to "0"
                    "margin" to "20px"
                }
            }
        }
    }
}