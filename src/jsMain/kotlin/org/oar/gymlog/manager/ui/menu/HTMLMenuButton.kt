package org.oar.gymlog.manager.ui.menu

import org.oar.gymlog.manager.custom.DefinitionConstants.DIV
import org.oar.gymlog.manager.custom.HTMLBlock
import org.oar.gymlog.manager.custom.style
import org.w3c.dom.HTMLDivElement

open class HTMLMenuButton(
    text: String,
    onClick: HTMLMenuButton.() -> Unit
): HTMLBlock<HTMLDivElement>(DIV, id = ID) {
    init {
        element.apply {
            textContent = text
            onclick = { this@HTMLMenuButton.onClick() }
        }
    }

    companion object {
        const val ID = "menu-button"
        init {
            style {
                "#$ID" {
                    "display" to "inline-block"
                    "width" to "90px"
                    "padding" to "7px 0"
                    "border" to "0 solid transparent"
                    "margin" to "3px 4px"
                    "background-color" to "#00000018"
                    "text-align" to "center"
                    "cursor" to "pointer"
                    "user-select" to "none"
                    "transition" to "background-color 0.3s"

                    "&:hover" {
                        "background-color" to "#cccccc"
                        "border-width" to "3px"
                        "margin" to "0 1px"
                    }

                    "&.selected" {
                        "background-color" to "#a1c6ff"
                        "border-width" to "3px"
                        "margin" to "0 1px"
                    }
                }
            }
        }
    }
}