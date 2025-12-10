package org.oar.gymlog.manager.ui.support

import org.oar.gymlog.manager.custom.DefinitionConstants.DIV
import org.oar.gymlog.manager.custom.HTMLBlock
import org.oar.gymlog.manager.custom.style
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement

open class HTMLPopup(
    id: String? = null,
    className: String? = null,
): HTMLBlock<HTMLDivElement>(DIV, id = id, className = "$CLASS_NAME closeable") {

    protected val content = DIV(className = "content")

    init {
        element.apply {
            if (className != null) classList.add(*className.split(" ").toTypedArray())

            onclick = {
                if ((it.target as HTMLElement).classList.contains("closeable")){
                    closePopup()
                }
            }
        }

        +DIV("closeable") {
            +content
        }
    }

    open fun closePopup() {
        clear(detach = true)
        parent!!.remove(this)
    }

    companion object {
        const val CLASS_NAME = "popup-dialog"
        init {
            style {
                ".$CLASS_NAME" {
                    "display" to "table"
                    "position" to "fixed"
                    "top" to "0"
                    "left" to "0"
                    "height" to "100%"
                    "width" to "100%"
                    "background-color" to "#141414c7"
                    "z-index" to "10"
                    "text-align" to "left"

                    "> div" {
                        "display" to "table-cell"
                        "vertical-align" to "middle"
                        "backdrop-filter" to "blur(1.5px) grayscale(0.3)"
                    }
                }
            }
        }
    }
}