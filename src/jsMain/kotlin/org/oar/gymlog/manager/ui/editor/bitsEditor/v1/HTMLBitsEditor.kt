package org.oar.gymlog.manager.ui.editor.bitsEditor.v1

import org.oar.gymlog.manager.Style.BUTTON_STYLE
import org.oar.gymlog.manager.model.OutputExercise
import org.oar.gymlog.manager.utils.Notifier
import org.oar.lib.HTMLBlock
import org.oar.lib.HTMLDefinitionConstants.BUTTON
import org.oar.lib.HTMLDefinitionConstants.DIV
import org.oar.lib.HTMLDefinitionConstants.H2
import org.oar.lib.style
import org.w3c.dom.HTMLDivElement

class HTMLBitsEditor(
    private val exercise: OutputExercise
): HTMLBlock<HTMLDivElement>(DIV, id = ID) {

    private val content: HTMLBlock<*> = DIV()

    init {
        +DIV("header") {
            +BUTTON("$BUTTON_STYLE big transparent") {
                element.onclick = {
                    notify(Notifier.editorExercisePanel)
                }
                !"&#9664;"
            }
            +H2 {
                -"#${exercise.exerciseId} ${exercise.name}"
            }
        }
        +content

        listen(Notifier.reload) {
            loadList()
        }
        loadList()
    }

    private fun loadList() {
        content.apply {
            clear()
            +HTMLTrainingList(exercise)
        }
    }

    companion object {
        const val ID = "editor-back"
        init {
            style {
                "#$ID" {
                    ".header" {
                        "> *" {
                            "vertical-align" to "middle"
                        }
                        "h2" {
                            "display" to "inline-block"
                            "margin" to "0 10px"
                        }
                    }
                }
            }
        }
    }
}