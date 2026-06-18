package org.oar.gymlog.manager.ui.editor.exerciseEditor.v1

import org.oar.gymlog.manager.Style.BUTTON_STYLE
import org.oar.gymlog.manager.model.Output
import org.oar.gymlog.manager.model.OutputExercise
import org.oar.gymlog.manager.ui.support.HTMLExerciseIcon
import org.oar.gymlog.manager.ui.support.HTMLInputEditBind
import org.oar.gymlog.manager.ui.support.HTMLSelectExerciseIconDialog
import org.oar.gymlog.manager.utils.Export
import org.oar.gymlog.manager.utils.Notifier
import org.oar.lib.HTMLBlock
import org.oar.lib.HTMLDefinitionConstants.BUTTON
import org.oar.lib.HTMLDefinitionConstants.DIV
import org.oar.lib.HTMLDefinitionConstants.H1
import org.oar.lib.HTMLDefinitionConstants.TABLE
import org.oar.lib.HTMLDefinitionConstants.TBODY
import org.oar.lib.HTMLDefinitionConstants.TD
import org.oar.lib.HTMLDefinitionConstants.TH
import org.oar.lib.HTMLDefinitionConstants.THEAD
import org.oar.lib.HTMLDefinitionConstants.TR
import org.oar.lib.style
import org.w3c.dom.HTMLDivElement

class HTMLExerciseEditor: HTMLBlock<HTMLDivElement>(DIV, id = ID) {

    private var output: Output = read(Export.output)!!
    private lateinit var exerciseSelected: OutputExercise

    init {
        listen(Notifier.exerciseSelected) {
            exerciseSelected = it
            load()
        }

        listen(Notifier.reload) {
            load()
        }
    }

    private fun load() {
        clear(detachMode = DetachMode.DETACH_ONLY_CHILDREN)
        +createExerciseBox(exerciseSelected)
        +DIV {
            +addVariations(exerciseSelected)
        }
    }

    private fun createExerciseBox(exercise: OutputExercise) = DIV {
        +H1 { -"Exercise #${exercise.exerciseId}" }

        +HTMLExerciseIcon(source = exercise.image, size = 64).apply {
            element.onclick = {
                this@HTMLExerciseEditor.append(
                    HTMLSelectExerciseIconDialog(exercise.image) {
                        if (it != null) {
                            exercise.image = it
                            setImageValue(it)
                        }
                    }
                )
            }
        }

        +HTMLInputEditBind(
            obj = exercise,
            accessor = OutputExercise::name,
            mapperBack = { it.trim() }
        ).apply { classList.add("name","subtle") }

        +TABLE {
            +THEAD {
                +TR {
                    +TH { -"Primary" }
                    +TH { -"Secondary" }
                }
            }
            +TBODY {
                +TR {
                    +TD("muscle-cell") {
                        +HTMLMuscleAssigner(output, exercise, output.primaries)
                    }
                    +TD("muscle-cell") {
                        +HTMLMuscleAssigner(output, exercise, output.secondaries, allowEmpty = true)
                    }
                }
            }
        }

        +BUTTON("$BUTTON_STYLE big") {
            element.onclick = {
                notify(Notifier.editorBitsPanel, exerciseSelected)
            }
//                -"↓↓"
            -"Find registries"
        }
    }

    private fun addVariations(exercise: OutputExercise)  = DIV {
        output.variations
            .filter { it.exerciseId == exercise.exerciseId }
            .forEach {
                +HTMLVariationEditor(output, it.variationId)
            }
    }

    companion object {
        const val ID = "exercise-editor"
        init {
            style {
                "#$ID" {
                    "h1" {
                        "margin-top" to 0
                    }

                    ".muscle-cell" {
                        "vertical-align" to "top"
                    }
                    ".name" {
                        "padding" to "10px"
                        "width" to "500px"
                        "margin-left" to "10px"
                        "font-size" to "16px"
                    }

                    ".exercise-icon" {
                        "cursor" to "pointer"
                    }
                }
            }
        }
    }
}