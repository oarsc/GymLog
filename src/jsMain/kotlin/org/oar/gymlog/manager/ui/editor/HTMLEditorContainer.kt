package org.oar.gymlog.manager.ui.editor

import org.oar.gymlog.manager.constants.ExportId
import org.oar.gymlog.manager.constants.NotifierId
import org.oar.gymlog.manager.custom.DefinitionConstants.DIV
import org.oar.gymlog.manager.custom.HTMLBlock
import org.oar.gymlog.manager.custom.style
import org.oar.gymlog.manager.model.OutputVariation
import org.oar.gymlog.manager.ui.editor.bitsEditor.v1.HTMLBitsEditor
import org.oar.gymlog.manager.ui.editor.exerciseEditor.HTMLSelector
import org.oar.gymlog.manager.ui.editor.exerciseEditor.v1.HTMLExerciseEditor
import org.w3c.dom.HTMLDivElement

class HTMLEditorContainer: HTMLBlock<HTMLDivElement>(DIV, id = ID) {

    private val exerciseEditorPanel: HTMLBlock<HTMLDivElement> = DIV()
    private var bitsEditorPanel: HTMLBlock<*>? = null

    init {
        val version =  read(ExportId.output)!!.version

        +exerciseEditorPanel.apply {
            +HTMLSelector()
            if (version == 1) {
                +HTMLExerciseEditor()
            }
        }

        listen(NotifierId.editorBitsPanel) {
            if (bitsEditorPanel == null) {
                -exerciseEditorPanel
                bitsEditorPanel = generateBitsPanel(it, version)
                +bitsEditorPanel!!
            }
        }

        listen(NotifierId.editorExercisePanel) {
            if (bitsEditorPanel != null) {
                +exerciseEditorPanel
                -bitsEditorPanel!!
                bitsEditorPanel = null
            }
        }
    }

    private fun generateBitsPanel(variation: OutputVariation, version: Int) = DIV {
        if (version == 1) {
            +HTMLBitsEditor(variation)
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