package org.oar.gymlog.manager.ui._common

import org.oar.gymlog.manager.lib.HTMLBlock
import org.oar.gymlog.manager.lib.HTMLDefinitionConstants.DIV
import org.oar.gymlog.manager.lib.HTMLDefinitionConstants.SPAN
import org.oar.gymlog.manager.lib.HTMLDefinitionConstants.TABLE
import org.oar.gymlog.manager.lib.HTMLDefinitionConstants.TBODY
import org.oar.gymlog.manager.lib.HTMLDefinitionConstants.TH
import org.oar.gymlog.manager.lib.HTMLDefinitionConstants.THEAD
import org.oar.gymlog.manager.lib.HTMLDefinitionConstants.TR
import org.oar.gymlog.manager.lib.style
import org.oar.gymlog.manager.model.Output
import org.oar.gymlog.manager.model.OutputBit
import org.oar.gymlog.manager.ui._common.HTMLCommonExerciseHistory.Column.ACTION
import org.oar.gymlog.manager.ui._common.HTMLCommonExerciseHistory.Column.INSTANT
import org.oar.gymlog.manager.ui._common.HTMLCommonExerciseHistory.Column.NOTE
import org.oar.gymlog.manager.ui._common.HTMLCommonExerciseHistory.Column.NUMBER
import org.oar.gymlog.manager.ui._common.HTMLCommonExerciseHistory.Column.REPS
import org.oar.gymlog.manager.ui._common.HTMLCommonExerciseHistory.Column.SUPER_SET
import org.oar.gymlog.manager.ui._common.HTMLCommonExerciseHistory.Column.TIMESTAMP
import org.oar.gymlog.manager.ui._common.HTMLCommonExerciseHistory.Column.VARIATION
import org.oar.gymlog.manager.ui._common.HTMLCommonExerciseHistory.Column.WEIGHT
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLTableCellElement
import org.w3c.dom.HTMLTableElement


open class HTMLCommonExerciseHistory(
    private val output: Output,
    protected val bits: MutableList<OutputBit>
): HTMLBlock<HTMLDivElement>(DIV, className = CLASS_NAME) {

    private val htmlBits = mutableListOf<HTMLCommonBitHistory>()
    open val columns = listOf(
        NUMBER, INSTANT, SUPER_SET, TIMESTAMP, REPS, WEIGHT, NOTE, VARIATION, ACTION
    )
    private lateinit var lastTableBlock: HTMLBlock<HTMLTableElement>

    override fun render(identifier: Int) {
        when (identifier) {
            -1 -> {
                +generateTableBlock()
            }
        }
    }

    open fun onAllBitsVariationChange(variationId: Int) { }

    private fun generateTableBlock(): HTMLBlock<HTMLTableElement> =
        TABLE {
            +THEAD {
                +TR {
                    +columnTitle(NUMBER, "#")
                    +columnTitle(INSTANT, "Instant")
                    +columnTitle(SUPER_SET, "SS", "Super Set")
                    +columnTitle(TIMESTAMP, "Timestamp")
                    +columnTitle(REPS, "Reps")
                    +columnTitle(WEIGHT, "Weight")
                    +columnTitle(NOTE, "Note")
                    +columnTitle(VARIATION, "Variation")
                    +columnTitle(ACTION, "Actions")
                }
            }
            +TBODY("bits") {
                var instants = 0
                bits.forEachIndexed { idx, it ->
                    val index = if (it.instant == true) {
                        instants++
                        -1
                    } else {
                        (idx-instants)+1
                    }

                    +HTMLCommonBitHistory(it, output, columns, ::changeAllBitsVariation).apply {
                        if (index >= 0) {
                            this.index = index
                        }
                        this.onUpdateInstant = { updateIndexes() }
                        htmlBits.add(this)
                    }
                }
            }
        }.apply { lastTableBlock = this }

    private fun changeAllBitsVariation(variationId: Int) {
        bits.forEach {
            it.variationId = variationId
        }

        -lastTableBlock
        lastTableBlock.clear(DetachMode.DETACH)
        +generateTableBlock()
        onAllBitsVariationChange(variationId)
    }

    private fun columnTitle(column: Column, text: String, title: String? = null): HTMLBlock<HTMLTableCellElement>? =
        if (columns.contains(column)) {
            TH {
                if (title != null) element.title = title
                +SPAN { -text }
            }
        } else {
            null
        }

    private fun updateIndexes() {
        var instants = 0
        bits.forEachIndexed { idx, it ->
            htmlBits[idx].index = if (it.instant == true) {
                instants++
                -1
            } else {
                (idx-instants)+1
            }
        }
    }

    enum class Column {
        NUMBER, INSTANT, SUPER_SET, TIMESTAMP, REPS, WEIGHT, NOTE, VARIATION, ACTION;
    }

    companion object {
        const val CLASS_NAME = "training-exercise"
        init {
            style {
                ".$CLASS_NAME" {
                    "margin-top" to "50px"
                    "&:first-child" {
                        "margin-top" to "20px"
                    }

                    "th span" {
                        "margin" to "0 5px"
                    }

                    "> *" {
                        "vertical-align" to "middle"
                    }

                    ".variation-id-label" {
                        "font-weight" to "bold"
                        "font-size" to "1.3em"
                        "margin" to "0 6px 0 13px"

                        "&::before" {
                            "content" to "\"[\""
                        }
                        "&::after" {
                            "content" to "\"] \""
                        }
                    }

                    ".variation-label" {
                        "color" to "#999"

                        "&::before" {
                            "content" to "\" - \""
                        }
                    }
                }
            }
        }
    }
}