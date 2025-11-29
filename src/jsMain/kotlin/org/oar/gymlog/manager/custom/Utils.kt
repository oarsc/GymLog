package org.oar.gymlog.manager.custom

import kotlinx.browser.window
import org.oar.gymlog.manager.custom.DefinitionConstants.HTMLDefinition
import org.w3c.dom.HTMLElement

object Utils {
    fun confirm(text: String) = window.confirm(text)

    fun setTimeout(delay: Int = 0, runnable: () -> Unit) = window.setTimeout(runnable, delay)
    fun clearTimeout(timeoutId: Int) = window.clearTimeout(timeoutId)

    fun setInterval(delay: Int, runnable: () -> Unit) = window.setInterval(runnable, delay)
    fun clearInterval(intervalId: Int) = window.clearInterval(intervalId)

    fun <T: HTMLElement> createBlock(
        htmlDefinition: HTMLDefinition<T>,
        className: String? = null,
        id: String? = null
    ) = object : HTMLBlock<T>(htmlDefinition, className = className, id = id) { }
}