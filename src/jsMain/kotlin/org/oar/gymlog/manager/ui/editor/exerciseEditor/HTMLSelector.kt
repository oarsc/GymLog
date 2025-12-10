package org.oar.gymlog.manager.ui.editor.exerciseEditor

import org.oar.gymlog.manager.Style.BUTTON_STYLE
import org.oar.gymlog.manager.lib.HTMLBlock
import org.oar.gymlog.manager.lib.HTMLDefinitionConstants.BUTTON
import org.oar.gymlog.manager.lib.HTMLDefinitionConstants.DIV
import org.oar.gymlog.manager.lib.style
import org.oar.gymlog.manager.model.Output
import org.oar.gymlog.manager.model.OutputExercise
import org.oar.gymlog.manager.ui.support.HTMLSelectExerciseDialog
import org.oar.gymlog.manager.utils.Export
import org.oar.gymlog.manager.utils.Notifier
import org.w3c.dom.HTMLDivElement

class HTMLSelector: HTMLBlock<HTMLDivElement>(DIV, id = ID) {

    private var output: Output = read(Export.output)!!
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
                notify(Notifier.exerciseSelected, exercise)
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