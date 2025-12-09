package org.oar.gymlog.manager.custom

import kotlinx.browser.document
import org.oar.gymlog.manager.custom.Utils.createBlock
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.HTMLBRElement
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLHeadingElement
import org.w3c.dom.HTMLImageElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLLabelElement
import org.w3c.dom.HTMLOptionElement
import org.w3c.dom.HTMLParagraphElement
import org.w3c.dom.HTMLSelectElement
import org.w3c.dom.HTMLSpanElement
import org.w3c.dom.HTMLStyleElement
import org.w3c.dom.HTMLTableCellElement
import org.w3c.dom.HTMLTableElement
import org.w3c.dom.HTMLTableRowElement
import org.w3c.dom.HTMLTableSectionElement
import kotlin.reflect.KClass

object DefinitionConstants {
    private val definitionByTag = mutableMapOf<String, HTMLDefinition<*>>()

    val A = HTMLDefinition("a", HTMLAnchorElement::class)
    val STYLE = HTMLDefinition("style", HTMLStyleElement::class)
    val INPUT = HTMLDefinition("input", HTMLInputElement::class)
    val BUTTON = HTMLDefinition("button", HTMLButtonElement::class)
    val DIV = HTMLDefinition("div", HTMLDivElement::class)
    val SPAN = HTMLDefinition("span", HTMLSpanElement::class)
    val IMG = HTMLDefinition("img", HTMLImageElement::class)
    val P = HTMLDefinition("p", HTMLParagraphElement::class)
    val H1 = HTMLDefinition("h1", HTMLHeadingElement::class)
    val H2 = HTMLDefinition("h2", HTMLHeadingElement::class)
    val H3 = HTMLDefinition("h3", HTMLHeadingElement::class)
    val BR = HTMLDefinition("br", HTMLBRElement::class)
    val TABLE = HTMLDefinition("table", HTMLTableElement::class)
    val TBODY = HTMLDefinition("tbody", HTMLTableSectionElement::class)
    val THEAD = HTMLDefinition("thead", HTMLTableSectionElement::class)
    val TD = HTMLDefinition("td", HTMLTableCellElement::class)
    val LABEL = HTMLDefinition("label", HTMLLabelElement::class)
    val TR = HTMLDefinition("tr", HTMLTableRowElement::class)
    val TH = HTMLDefinition("th", HTMLTableCellElement::class)
    val SELECT = HTMLDefinition("select", HTMLSelectElement::class)
    val OPTION = HTMLDefinition("option", HTMLOptionElement::class)

    fun String.toDefinition(): HTMLDefinition<*> = definitionByTag[this]!!

    data class HTMLDefinition<T : HTMLElement>(val tagName: String, val cls: KClass<T>) {
        init {
            definitionByTag[tagName] = this
        }
        fun create(): T = document.createElement(tagName) as T

        operator fun invoke(className: String? = null, id: String? = null, build: HTMLBlock<T>.() -> Unit): HTMLBlock<T> =
            createBlock(this).apply {
                if (className != null) element.className = className
                if (id != null) element.id = id
                build()
            }
    }
}