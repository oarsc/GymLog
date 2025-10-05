package org.oar.gymlog.ui.exercises

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oar.gymlog.databinding.ListitemExercisesRowBinding
import org.oar.gymlog.databinding.ListitemExercisesVariationBinding
import org.oar.gymlog.model.Exercise
import org.oar.gymlog.model.Muscle
import org.oar.gymlog.model.Order
import org.oar.gymlog.model.Variation
import org.oar.gymlog.ui.common.animations.ResizeHeightAnimation
import org.oar.gymlog.ui.common.components.listView.CommonListView.ListElementState
import org.oar.gymlog.ui.common.components.listView.SimpleListHandler
import org.oar.gymlog.ui.common.components.listView.SimpleListView
import org.oar.gymlog.ui.main.preferences.PreferencesDefinition
import org.oar.gymlog.util.Constants.TODAY
import org.oar.gymlog.util.DateUtils.getLetterFrom
import org.oar.gymlog.util.extensions.PreferencesExts.loadString
import java.util.Locale
import java.util.function.BiConsumer
import java.util.function.Consumer


class ExercisesListHandler(
    val context: Context,
    private val simpleListView: SimpleListView<Exercise, ListitemExercisesRowBinding>,
    private val muscle: Muscle? = null
): SimpleListHandler<Exercise, ListitemExercisesRowBinding> {
    private var onExerciseClickListener: BiConsumer<Exercise, Boolean>? = null

    fun onExerciseClicked(listener: BiConsumer<Exercise, Boolean>) {
        onExerciseClickListener = listener
    }
    fun onVariationClicked(listener: Consumer<Variation>) {
        VariationsListHandler.onVariationClicked(listener)
    }

    fun init() {
        updateOrder(
            Order.getByCode(context.loadString(PreferencesDefinition.EXERCISES_ORDER))
        )
    }

    override val useListState = true
    override val itemInflater: (LayoutInflater, ViewGroup?, Boolean) -> ListitemExercisesRowBinding
        = ListitemExercisesRowBinding::inflate


    override fun buildListView(
        binding: ListitemExercisesRowBinding,
        item: Exercise,
        index: Int,
        state: ListElementState?
    ) {
        state!!

        binding.header.apply {
            exerciseName.text = item.name
            variationName.visibility = View.GONE

            time.apply {
                val timeStr = TODAY.getLetterFrom(item.lastTrained)
                visibility = if (timeStr.isEmpty()) View.GONE else View.VISIBLE
                text = timeStr
            }

            image.setImage(item.image, muscle?.color ?: item.primaryMuscles[0].color)
        }

        binding.variationsList.visibility = if (state["expanded", false])
            View.VISIBLE else
            View.GONE

        val variations = item.gymVariations
        if (variations.size > 1) {
            @Suppress("UNCHECKED_CAST")
            val variationList =
                binding.variationsList as SimpleListView<Variation, ListitemExercisesVariationBinding>

            variationList.init(variations, VariationsListHandler)

            binding.header.root.setOnClickListener {
                val expanded = !state["expanded", false]

                val anim = if (expanded) {
                    ResizeHeightAnimation(variationList)
                } else {
                    ResizeHeightAnimation(variationList, 0)
                }

                variationList.startAnimation(anim)
                state["expanded"] = expanded
            }

        } else {
            onExerciseClickListener?.also { listener ->
                binding.header.root.setOnClickListener { listener.accept(item, false) }
            }
        }
        onExerciseClickListener?.also { listener ->
            binding.header.root.setOnLongClickListener {
                listener.accept(item, true)
                true
            }
        }
    }

    fun updateOrder(order: Order) {
        val alphabeticalComparator =
            Comparator.comparing { e: Exercise -> e.name.lowercase(Locale.getDefault()) }

        val dateComparator by lazy {
            Comparator.comparing { e: Exercise -> e.lastTrained }
        }

        val comparator = when (order) {
            Order.ALPHABETICALLY -> alphabeticalComparator
            Order.LAST_USED -> dateComparator.reversed().thenComparing(alphabeticalComparator)
        }
        simpleListView.sort(comparator)
    }
}