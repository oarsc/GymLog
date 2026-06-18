package org.oar.gymlog.manager

import org.oar.gymlog.manager.ui.calendar.HTMLCalendarContainer
import org.oar.gymlog.manager.ui.editor.HTMLEditorContainer
import org.oar.gymlog.manager.ui.input.HTMLInputContainer
import org.oar.gymlog.manager.ui.menu.HTMLMenu
import org.oar.gymlog.manager.ui.menu.HTMLMenu.Companion.CALENDAR_OPTION
import org.oar.gymlog.manager.ui.menu.HTMLMenu.Companion.EDITOR_OPTION
import org.oar.gymlog.manager.utils.Export
import org.oar.gymlog.manager.utils.Notifier
import org.oar.lib.HTMLBlock
import org.oar.lib.HTMLBlock.Companion.HTMLBodyBlock
import org.oar.lib.HTMLBlock.Companion.listen
import org.oar.lib.HTMLBlock.Companion.read
import org.oar.lib.HTMLBlock.DetachMode
import org.oar.lib.HTMLDefinitionConstants.DIV
import org.w3c.dom.HTMLDivElement

fun main() {
    Style.load()

    val divContainer = DIV()

    HTMLBodyBlock.apply {
        +HTMLMenu()
        +HTMLInputContainer()
        +divContainer

        listen(Notifier.fileLoaded) {
            detachAll(listeners = true)

            listen(Notifier.fileLoaded) {
                divContainer.loadContent()
            }

            listen(Notifier.menuIdChanged) {
                divContainer.loadContent(it)
            }

            divContainer.loadContent()
        }
    }
}

fun HTMLBlock<HTMLDivElement>.loadContent(
    content: Int = read(Export.menuId)!!
) {
    clear(detachMode = DetachMode.DETACH_ONLY_CHILDREN)
    when(content) {
        CALENDAR_OPTION -> +HTMLCalendarContainer()
        EDITOR_OPTION -> +HTMLEditorContainer()
    }
}
