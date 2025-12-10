package org.oar.gymlog.manager.ui.support

import org.oar.gymlog.manager.lib.HTMLBlock
import org.oar.gymlog.manager.lib.HTMLDefinitionConstants.DIV
import org.oar.gymlog.manager.lib.HTMLDefinitionConstants.IMG
import org.oar.gymlog.manager.lib.style
import org.w3c.dom.HTMLDivElement

class HTMLExerciseIcon(
    source: String,
    private val hue: Int? = null,
    size: Int = 64
): HTMLBlock<HTMLDivElement>(DIV, className = CLASS_NAME) {
    private val preview = IMG()
    private val mask by lazy { IMG(className = "mask") }

    init {
        val sizePx = "${size}px"
        setSize(sizePx)
        preview.element.src = "./previews/$source.png"
        preview.setSize(sizePx)

        +DIV("icon-block") {
            setSize(sizePx)

            +preview
            if (hue != null) {
                +mask.apply {
                    element.src = "./masks/$source.png"
                    element.style.filter = "brightness(70%) sepia(100%) hue-rotate(${hue}deg)"
                    setSize(sizePx)
                }
            }
        }
    }

    private fun HTMLBlock<*>.setSize(size: String) {
        element.style.apply {
            width = size
            height = size
        }
    }

    fun setImageValue(source: String) {
        preview.element.src = "./previews/$source.png"
        if (hue != null) {
            mask.element.src = "./masks/$source.png"
        }
    }

    companion object {
        const val CLASS_NAME = "exercise-icon"
        init {
            style {
                ".$CLASS_NAME" {
                    "display" to "inline-block"

                    ".icon-block" {
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
}