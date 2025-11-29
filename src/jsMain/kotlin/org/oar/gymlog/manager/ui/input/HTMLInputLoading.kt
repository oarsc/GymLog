package org.oar.gymlog.manager.ui.input

import org.oar.gymlog.manager.custom.DefinitionConstants.P
import org.oar.gymlog.manager.custom.HTMLBlock
import org.oar.gymlog.manager.custom.style
import org.w3c.dom.HTMLParagraphElement

class HTMLInputLoading: HTMLBlock<HTMLParagraphElement>(P, id = ID) {

    init {
        -"Loading..."
    }

    companion object {
        const val ID = "input-loading"
        init {
            style {
                "#$ID" {
                    "display" to "none"
                }
            }
        }
    }
}