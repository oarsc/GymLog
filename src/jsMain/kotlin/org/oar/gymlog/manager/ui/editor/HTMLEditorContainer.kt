package org.oar.gymlog.manager.ui.editor

import org.oar.gymlog.manager.constants.ExportId
import org.oar.gymlog.manager.custom.DefinitionConstants.DIV
import org.oar.gymlog.manager.custom.HTMLBlock
import org.oar.gymlog.manager.custom.style
import org.oar.gymlog.manager.ui.editor.v1.HTMLExerciseEditor
import org.w3c.dom.HTMLDivElement

class HTMLEditorContainer: HTMLBlock<HTMLDivElement>(DIV, id = ID) {
    init {
        val version =  read(ExportId.output)!!.version

        +HTMLSelector()
        if (version == 1) {
            +HTMLExerciseEditor()
        }
    }

    companion object {
        const val ID = "editor-container"
        init {
            style {
                "#$ID" {
                    "margin" to "50px auto"
                    "width" to "750px"
                    "padding" to "20px"
                    "background-color" to "#f5f5f5"
                    "border" to "1px solid black"
                    "position" to "relative"

                    "min-height" to "36px"
                }
            }
        }
    }
}