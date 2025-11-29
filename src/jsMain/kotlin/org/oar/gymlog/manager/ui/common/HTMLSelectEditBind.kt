package org.oar.gymlog.manager.ui.common

import org.oar.gymlog.manager.custom.DefinitionConstants.OPTION
import org.oar.gymlog.manager.custom.DefinitionConstants.SELECT
import org.oar.gymlog.manager.custom.HTMLBlock
import org.oar.gymlog.manager.custom.style
import org.w3c.dom.HTMLSelectElement
import kotlin.reflect.KMutableProperty1

class HTMLSelectEditBind<T: Any, O>(
    private val obj: T,
    private val accessor: KMutableProperty1<T, O>,
    options: Map<String, String>,
    private val mapperBack: String.() -> O,
    mapper: O.() -> String = { this?.toString() ?: "" }
): HTMLBlock<HTMLSelectElement>(SELECT, className = CLASS_NAME) {

    var onchange: ((O) -> Unit)? = null

    init {
        options.forEach { (key, title) ->
            +OPTION {
                element.apply {
                    value = key
                    textContent = title
                }
            }
        }

        element.apply {
            value = accessor.get(obj).mapper()
            onchange = {
                val originalValue = value.mapperBack()
                this@HTMLSelectEditBind.accessor.set(obj, originalValue)
                this@HTMLSelectEditBind.onchange?.invoke(originalValue)
            }
        }
    }

    companion object {
        const val CLASS_NAME = "select"
        init {
            style {
                ".CLASS_NAME" {
                }
            }
        }
    }
}