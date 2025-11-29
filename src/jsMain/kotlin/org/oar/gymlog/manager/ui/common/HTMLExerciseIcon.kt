package org.oar.gymlog.manager.ui.common

import org.oar.gymlog.manager.custom.DefinitionConstants.DIV
import org.oar.gymlog.manager.custom.DefinitionConstants.IMG
import org.oar.gymlog.manager.custom.HTMLBlock
import org.oar.gymlog.manager.custom.Utils.createBlock
import org.oar.gymlog.manager.custom.style
import org.w3c.dom.HTMLDivElement

class HTMLExerciseIcon(
    source: String,
    hue: Int? = null,
    size: Int = 64
): HTMLBlock<HTMLDivElement>(DIV, className = CLASS_NAME) {

    private val preview = createBlock(IMG)
    private val mask by lazy { createBlock(IMG, className = "mask") }

    init {
        val sizePx = "${size}px"
        element.style.apply {
            width = sizePx
            height = sizePx
        }
        preview.element.src = "./previews/$source.png"
        preview.element.style.apply {
            width = sizePx
            height = sizePx
        }

        +preview
        if (hue != null) {
            +mask.apply {
                element.src = "./masks/$source.png"
                element.style.apply {
                    filter = "brightness(70%) sepia(100%) hue-rotate(${hue}deg)"
                    width = sizePx
                    height = sizePx
                }
            }
        }
    }


    companion object {
        const val CLASS_NAME = "exercise-icon"
        init {
            style {
                ".$CLASS_NAME" {
                    "position" to "relative"
                    "display" to "inline-block"
                    "vertical-align" to "middle"

                    "img" {
                        "position" to "absolute"
                        "top" to 0
                        "left" to 0
                    }
                }
            }
        }
    }
}