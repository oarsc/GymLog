package org.oar.gymlog.manager.ui.editor.bitsEditor.v1

import org.oar.gymlog.manager.model.Output
import org.oar.gymlog.manager.model.OutputBit
import org.oar.gymlog.manager.ui._common.HTMLCommonExerciseHistory

class HTMLEditorExerciseHistory(
    output: Output,
    bits: MutableList<OutputBit>
) : HTMLCommonExerciseHistory(output, bits) {
    override val columns = super.columns - Column.ACTION
}