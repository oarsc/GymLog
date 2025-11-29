package org.oar.gymlog.manager.ui.common

import org.oar.gymlog.manager.custom.DefinitionConstants.INPUT
import org.oar.gymlog.manager.custom.HTMLBlock
import org.oar.gymlog.manager.custom.style
import org.w3c.dom.HTMLInputElement
import kotlin.reflect.KMutableProperty1

class HTMLCheckboxEditBind<T: Any>(
    private val obj: T,
    private val accessor: KMutableProperty1<T, Boolean?>,
    private val default: Boolean = false
): HTMLBlock<HTMLInputElement>(INPUT, className = CLASS_NAME) {

    var onchange: (() -> Unit)? = null

    init {
        element.apply {
            type = "checkbox"
            checked = accessor.get(obj) ?: default
            onchange = {
                this@HTMLCheckboxEditBind.accessor.set(obj, checked)
                this@HTMLCheckboxEditBind.onchange?.invoke()
            }
        }
    }

    companion object {
        const val CLASS_NAME = "checkbox"
        init {
            style {
                ".$CLASS_NAME" {

                }
            }
        }
    }
}