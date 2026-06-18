package org.oar.gymlog.manager.ui.editor

import org.oar.gymlog.manager.model.OutputExercise
import org.oar.gymlog.manager.ui.editor.bitsEditor.v1.HTMLBitsEditor
import org.oar.gymlog.manager.ui.editor.exerciseEditor.HTMLSelector
import org.oar.gymlog.manager.ui.editor.exerciseEditor.v1.HTMLExerciseEditor
import org.oar.gymlog.manager.utils.Export
import org.oar.gymlog.manager.utils.Notifier
import org.oar.lib.HTMLBlock
import org.oar.lib.HTMLDefinitionConstants.DIV
import org.oar.lib.style
import org.w3c.dom.HTMLDivElement

class HTMLEditorContainer: HTMLBlock<HTMLDivElement>(DIV, id = ID) {

    private val exerciseEditorPanel: HTMLBlock<HTMLDivElement> = DIV()
    private var bitsEditorPanel: HTMLBlock<*>? = null

    init {
        val version =  read(Export.output)!!.version

        +exerciseEditorPanel.apply {
            +HTMLSelector()
            if (version == 1) {
                +HTMLExerciseEditor()
            }
        }

        listen(Notifier.editorBitsPanel) {
            if (bitsEditorPanel == null) {
                -exerciseEditorPanel
                bitsEditorPanel = generateBitsPanel(it, version)
                +bitsEditorPanel!!
            }
        }

        listen(Notifier.editorExercisePanel) {
            if (bitsEditorPanel != null) {
                +exerciseEditorPanel
                -bitsEditorPanel!!
                bitsEditorPanel = null
            }
        }
    }

    private fun generateBitsPanel(exercise: OutputExercise, version: Int) = DIV {
        if (version == 1) {
            +HTMLBitsEditor(exercise)
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