package org.oar.gymlog.manager.ui.calendar


import org.oar.gymlog.manager.lib.HTMLBlock
import org.oar.gymlog.manager.lib.HTMLDefinitionConstants.DIV
import org.oar.gymlog.manager.lib.style
import org.oar.gymlog.manager.ui.calendar.v1.HTMLTrainingSummary
import org.oar.gymlog.manager.utils.Export
import org.w3c.dom.HTMLDivElement

class HTMLCalendarContainer: HTMLBlock<HTMLDivElement>(DIV, id = ID) {
    init {
        val version =  read(Export.output)!!.version

        if (version == 1) {
            +HTMLTrainingSummary()
        }
    }

    companion object {
        const val ID = "calendar-container"
        init {
            style {
                "#$ID" {
                    "margin" to "50px auto"
                    "width" to "750px"
                    "padding" to "20px"
                    "background-color" to "#f5f5f5"
                    "border" to "1px solid black"
                    "position" to "relative"
                }
            }
        }
    }
}