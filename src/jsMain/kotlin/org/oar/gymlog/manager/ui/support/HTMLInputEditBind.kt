package org.oar.gymlog.manager.ui.support

import org.oar.lib.HTMLBlock
import org.oar.lib.HTMLDefinitionConstants.INPUT
import org.oar.lib.style
import org.w3c.dom.HTMLInputElement
import kotlin.reflect.KMutableProperty1

class HTMLInputEditBind<T: Any, O>(
    private val obj: T,
    private val accessor: KMutableProperty1<T, O>,
    private val mapperBack: HTMLInputEditBind<T, O>.(String) -> O,
    mapper: O.() -> String = { this?.toString() ?: "" }
): HTMLBlock<HTMLInputElement>(INPUT, className = CLASS_NAME) {

    init {
        element.apply {
            value = accessor.get(obj).mapper()
            onblur = {
                this@HTMLInputEditBind.accessor.set(obj, mapperBack(value))
            }
        }
    }

    companion object {
        const val CLASS_NAME = "input"
        init {
            style {
                ".$CLASS_NAME" {
                }
            }
        }
    }
}