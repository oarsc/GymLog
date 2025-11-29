package org.oar.gymlog.manager.ui.menu

import org.oar.gymlog.manager.constants.ExportId
import org.oar.gymlog.manager.constants.NotifierId
import org.oar.gymlog.manager.custom.DefinitionConstants.DIV
import org.oar.gymlog.manager.custom.HTMLBlock
import org.oar.gymlog.manager.custom.style
import org.oar.gymlog.manager.process.Export
import org.w3c.dom.HTMLDivElement

class HTMLMenu: HTMLBlock<HTMLDivElement>(DIV, id = ID) {

    private var selectedOption: Int by renderProperty(CALENDAR_OPTION, 1)
    private var menuItems = mapOf(
        CALENDAR_OPTION to HTMLMenuContentButton("Calendar", CALENDAR_OPTION),
        EDITOR_OPTION to HTMLMenuContentButton("Editor", EDITOR_OPTION)
    )

    init {
        listen(NotifierId.menuIdChanged) { selectedOption = it }
        expose(ExportId.menuId) { selectedOption }

        menuItems.forEach {
            +it.value
            if (it.key == selectedOption) {
                it.value.selected = true
            }
        }

        +createLoadFileButton()
        +createExportButton()
    }

    private fun createLoadFileButton() = HTMLMenuButton(
        text = "Load file",
        onClick = {
            classList.toggle("selected")
            notify(NotifierId.showLoadFile, true)
        }
    ).apply {
        classList.add("selected")

        listen(NotifierId.fileLoaded) {
            classList.toggle("selected", false)
        }
    }

    private fun createExportButton() = HTMLMenuButton(
        text = "Export",
        onClick = {
            read(ExportId.output)?.also(Export::export)
            notify(NotifierId.reload)
        }
    )

    override fun render(identifier: Int) {
        when (identifier) {
            1 -> {
                menuItems.forEach {
                    it.value.selected = it.key == selectedOption
                }
            }
        }
    }

    companion object {
        const val CALENDAR_OPTION = 1
        const val EDITOR_OPTION = 2

        const val ID = "menu"
        init {
            style {
                "#$ID" {
                    "padding" to "10px 0 0"
                    "text-align" to "center"
                }
            }
        }
    }
}