package org.oar.gymlog.manager.constants

import org.oar.gymlog.manager.model.Output

object ExportId {
    val inputId = object : ExportId<String>() {}
    val output = object : ExportId<Output>() {}
    val menuId = object : ExportId<Int>() {}

    open class ExportId<T: Any> internal constructor()
}