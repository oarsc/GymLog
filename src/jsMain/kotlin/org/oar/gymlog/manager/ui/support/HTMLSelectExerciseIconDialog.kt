package org.oar.gymlog.manager.ui.support

import org.oar.gymlog.manager.lib.HTMLBlock
import org.oar.gymlog.manager.lib.HTMLDefinitionConstants.DIV
import org.oar.gymlog.manager.lib.style
import org.oar.gymlog.manager.utils.Utils.fetchText
import org.oar.gymlog.manager.utils.Utils.setTimeout
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.ScrollToOptions
import org.w3c.dom.get

class HTMLSelectExerciseIconDialog(
    private val initialIcon: String? = null,
    private val onSelectIcon: (String?) -> Unit,
): HTMLPopup(id = ID) {

    private val list = DIV(className = "exercise-icon-list")

    init {
        content.apply {
            +list.apply {
                if (IMAGES.isEmpty()) {
                    fetchText("preview.indexes") { text ->
                        text.split("\n")
                            .filter(String::isNotBlank)
                            .map { it.split(".").first() }
                            .also(IMAGES::addAll)
                        loadContent()
                        focus()
                    }
                } else {
                    loadContent()
                    focus()
                }
            }
        }
    }

    private fun HTMLBlock<HTMLDivElement>.loadContent() {
        IMAGES.forEach { image ->
            +HTMLExerciseIcon(source = image, size = 64).apply {
                element.apply {
                    if (image == initialIcon) {
                        classList.add("selected")
                    }
                    onclick = {
                        submit(image)
                    }
                }
            }
        }
    }

    private fun focus() {
        if (initialIcon != null) {
            val selectedElements = list.element.getElementsByClassName("selected")
            if (selectedElements.length > 0) {
                val elem = selectedElements[0] as HTMLElement
                setTimeout {
                    list.element.scrollTo(
                        ScrollToOptions(
                            top = elem.offsetTop - list.element.offsetTop - 128.0
                        )
                    )
                }
            }
        }
    }

    override fun closePopup() = submit(null)

    private fun submit(id: String? = null) {
        super.closePopup()
        onSelectIcon(id)
    }

    companion object {
        private val IMAGES = mutableListOf<String>()
        const val ID = "exercise-icon-dialog"
        init {
            style {
                "#$ID .content" {
                    "display" to "flex"
                    "width" to "750px"
                    "height" to "min(100vh, 900px)"
                    "min-height" to "200px"
                    "margin" to "auto"
                    "background-color" to "white"

                    ".exercise-icon-list" {
                        "text-align" to "center"
                        "overflow" to "auto"

                        ".exercise-icon" {
                            "padding" to "8px 4px"
                            "cursor" to "pointer"
                            "border" to "2px solid transparent"

                            "&:hover, &.selected" {
                                "border-color" to "#658dcb"
                                "background-color" to "#edf4ff"
                            }
                        }
                    }
                }
            }
        }
    }
}