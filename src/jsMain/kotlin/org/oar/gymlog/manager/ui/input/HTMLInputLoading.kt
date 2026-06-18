package org.oar.gymlog.manager.ui.input

import org.oar.lib.HTMLBlock
import org.oar.lib.HTMLDefinitionConstants.P
import org.oar.lib.style
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