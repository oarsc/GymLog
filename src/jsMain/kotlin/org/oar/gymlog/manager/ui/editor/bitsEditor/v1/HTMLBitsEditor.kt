package org.oar.gymlog.manager.ui.editor.bitsEditor.v1

import org.oar.gymlog.manager.Style.BUTTON_STYLE
import org.oar.gymlog.manager.constants.NotifierId
import org.oar.gymlog.manager.custom.DefinitionConstants.BUTTON
import org.oar.gymlog.manager.custom.DefinitionConstants.DIV
import org.oar.gymlog.manager.custom.DefinitionConstants.H1
import org.oar.gymlog.manager.custom.HTMLBlock
import org.oar.gymlog.manager.custom.style
import org.oar.gymlog.manager.model.OutputVariation
import org.w3c.dom.HTMLDivElement

class HTMLBitsEditor(
    private val variation: OutputVariation
): HTMLBlock<HTMLDivElement>(DIV, id = ID) {

    init {
        +DIV("header") {
            +BUTTON("$BUTTON_STYLE big transparent") {
                element.onclick = {
                    notify(NotifierId.editorExercisePanel)
                }
                !"&#9664;"
            }
            +H1 {
                if (variation.def) {
                    -"Default variation #${variation.variationId}"
                } else {
                    -"Variation #${variation.variationId}"
                }
            }
        }
        +HTMLTrainingList(variation)
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
                        "h1" {
                            "display" to "inline-block"
                            "margin" to "0 10px"
                        }
                    }
                }
            }
        }
    }
}