package org.oar.gymlog.manager.ui

import kotlinx.browser.document
import kotlinx.browser.window
import org.oar.gymlog.manager.custom.DefinitionConstants.DIV
import org.oar.gymlog.manager.custom.HTMLBlock
import org.oar.gymlog.manager.custom.style
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.SMOOTH
import org.w3c.dom.ScrollBehavior
import org.w3c.dom.ScrollToOptions

class LeftScroller : HTMLBlock<HTMLDivElement>(DIV, id = "scroller") {

    init {
        element.apply {
            val classLists = listOf(classList, document.body!!.classList)
            onmouseover =  {
                classLists.forEach { it.add("expanded") }
            }
            onmouseleave =  {
                classLists.forEach { it.remove("expanded") }
            }
            onmousedown = { ev ->
                val btn = ev.buttons.toInt()

                if (btn == 4) {} // middle click
                else if (btn == 2) { // right click
                    scrollScreen(-SCROLL_SIZE)
                } else {
                    scrollScreen(SCROLL_SIZE)
                }

                ev.stopPropagation()
                ev.preventDefault()
                false
            }
            oncontextmenu = {
                it.stopPropagation()
                it.preventDefault()
                false
            }
        }
    }

    private fun scrollScreen(value: Double) {
        window.scrollTo(
            ScrollToOptions(
                top = window.scrollY + value,
                behavior = ScrollBehavior.SMOOTH
            )
        )
    }

    companion object {
        private const val SCROLL_SIZE = 96.0 * 4
        private const val SCROLLER_APPEAR_SPEED = 0.5
        private const val SCROLLER_MIN_SIZE = 1
        private const val SCROLLER_MAX_SIZE = 40
        init {
            style {
                "#scroller" {
                    "position" to "fixed"
                    "top" to "0"
                    "bottom" to "0"
                    "left" to "0"
                    "width" to "${SCROLLER_MIN_SIZE}px"
                    "backgroundColor" to "#244464"
                    "opacity" to 0
                    "transition" to "width ${SCROLLER_APPEAR_SPEED}s, opacity ${SCROLLER_APPEAR_SPEED/2}s"
                    "boxShadow" to "inset -6px 0 5px -5px black"
                    "cursor" to "pointer"
                }
                "#scroller.expanded" {
                    "width" to "${SCROLLER_MAX_SIZE}px"
                    "opacity" to 1
                }

                "body" {
                    "transition" to "margin-left ${SCROLLER_APPEAR_SPEED}s"
                }
                "body.expanded" {
                    "margin-left" to "${SCROLLER_MAX_SIZE}px"
                }
            }
        }
    }
}
