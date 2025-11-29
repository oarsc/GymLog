package org.oar.gymlog.manager

import org.oar.gymlog.manager.constants.ExportId
import org.oar.gymlog.manager.constants.NotifierId
import org.oar.gymlog.manager.custom.DefinitionConstants.DIV
import org.oar.gymlog.manager.custom.HTMLBlock
import org.oar.gymlog.manager.custom.HTMLBlock.Companion.HTMLBodyBlock
import org.oar.gymlog.manager.custom.HTMLBlock.Companion.listen
import org.oar.gymlog.manager.custom.HTMLBlock.Companion.read
import org.oar.gymlog.manager.custom.Utils.createBlock
import org.oar.gymlog.manager.ui.calendar.HTMLCalendarContainer
import org.oar.gymlog.manager.ui.editor.HTMLEditorContainer
import org.oar.gymlog.manager.ui.input.HTMLInputContainer
import org.oar.gymlog.manager.ui.menu.HTMLMenu
import org.oar.gymlog.manager.ui.menu.HTMLMenu.Companion.CALENDAR_OPTION
import org.oar.gymlog.manager.ui.menu.HTMLMenu.Companion.EDITOR_OPTION
import org.w3c.dom.HTMLDivElement

fun main() {
    Style.load()

    val divContainer = createBlock(DIV)

    HTMLBodyBlock.apply {
        +HTMLMenu()
        +HTMLInputContainer()
        +divContainer

        listen(NotifierId.fileLoaded) {
            detachAll(listeners = true)

            listen(NotifierId.fileLoaded) {
                divContainer.loadContent()
            }

            listen(NotifierId.menuIdChanged) {
                divContainer.loadContent(it)
            }

            divContainer.loadContent()
        }
    }
}

fun HTMLBlock<HTMLDivElement>.loadContent(
    content: Int = read(ExportId.menuId)!!
) {
    clear(detach = true)
    when(content) {
        CALENDAR_OPTION -> +HTMLCalendarContainer()
        EDITOR_OPTION -> +HTMLEditorContainer()
    }
}
