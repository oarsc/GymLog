package org.scp.gymlog.ui.training

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentActivity
import androidx.viewbinding.ViewBinding
import org.scp.gymlog.R
import org.scp.gymlog.databinding.*
import org.scp.gymlog.exceptions.LoadException
import org.scp.gymlog.model.Bit
import org.scp.gymlog.model.Muscle
import org.scp.gymlog.model.Variation
import org.scp.gymlog.ui.common.CustomAppCompatActivity
import org.scp.gymlog.ui.common.components.listView.MultipleListHandler
import org.scp.gymlog.ui.common.components.listView.MultipleListView
import org.scp.gymlog.ui.common.components.listView.SimpleListView
import org.scp.gymlog.ui.common.dialogs.EditBitLogDialogFragment
import org.scp.gymlog.ui.common.dialogs.TextSelectDialogFragment
import org.scp.gymlog.ui.training.rows.ITrainingRow
import org.scp.gymlog.util.extensions.DatabaseExts.dbThread
import org.scp.gymlog.util.extensions.RedirectionExts.goToVariation
import java.io.IOException
import java.util.function.Consumer

class TrainingListHandler(
    val context: Context,
    private val internationalSystem: Boolean,
    var preExpandedIndex: Int? = null,
) : MultipleListHandler<ExerciseRows> {
    override val useListState = true

    private var onBitChangedListener: Consumer<Bit>? = null

    fun setOnBitChangedListener(onBitChangedListener: Consumer<Bit>) {
        this.onBitChangedListener = onBitChangedListener
    }

    override fun generateListItemInflater(item: ExerciseRows): (LayoutInflater, ViewGroup?, Boolean) -> ViewBinding {
        return if (item.superSet == null)
            ListitemHistoryExerciseHeaderBinding::inflate
        else
            ListitemHistorySupersetExerciseHeaderBinding::inflate
    }

    @Suppress("UNCHECKED_CAST")
    override fun buildListView(
        binding: ViewBinding,
        item: ExerciseRows,
        index: Int,
        state: SimpleListView.ListElementState?
    ) {
        state!!

        if (preExpandedIndex == index) {
            preExpandedIndex = null
            state["expanded"] = true
        }

        val bitList: MultipleListView<ITrainingRow>

        if (item.superSet == null) {
            binding as ListitemHistoryExerciseHeaderBinding

            bitList = binding.bitList as MultipleListView<ITrainingRow>
            bitList.unScrollableVertically = true

            val bitHandler = TrainingBitListHandler(internationalSystem)
            bitList.init(item, bitHandler)
            bitHandler.setOnClickListener { bit, idx ->
                bitClicked(bit, idx, bitList)
            }

            val exercise = item.exercise

            binding.row.title.text = exercise.name
            binding.row.subtitle.text = exercise.primaryMuscles
                .map(Muscle::text)
                .map { id: Int -> context.resources.getString(id) }
                .joinToString { it }

            binding.row.indicator.setCardBackgroundColor(
                ResourcesCompat.getColor(context.resources, exercise.primaryMuscles[0].color, null))

            val fileName = "previews/" + exercise.image + ".png"
            try {
                val ims = context.assets.open(fileName)
                val d = Drawable.createFromStream(ims, null)
                binding.row.image.setImageDrawable(d)

            } catch (e: IOException) {
                throw LoadException("Could not read \"$fileName\"", e)
            }

            binding.header.setOnClickListener { toggleBits(bitList, state) }

            binding.row.image.setOnLongClickListener {
                val variations = exercise.gymVariations
                val context = binding.root.context as CustomAppCompatActivity

                if (variations.size > 1) {
                    TextSelectDialogFragment(variations.map { it.name }) { pos, _ ->
                        if (pos != TextSelectDialogFragment.DIALOG_CLOSED) {
                            context.goToVariation(variations[pos])
                        }
                    }.apply { show(context.supportFragmentManager, null) }
                } else {
                    context.goToVariation(variations[0])
                }
                true
            }

        } else {
            binding as ListitemHistorySupersetExerciseHeaderBinding

            bitList = binding.bitList as MultipleListView<ITrainingRow>
            bitList.unScrollableVertically = true

            val bitHandler = TrainingBitListHandler(internationalSystem, item.variations)
            bitList.init(item, bitHandler)
            bitHandler.setOnClickListener { bit, idx ->
                bitClicked(bit, idx, bitList)
            }

            binding.superSetNumber.text = item.superSet.toString()

            val superSetExerciseListHandler = TrainingSuperSerHeaderHandler(context)
            val exerciseList = binding.exerciseList as SimpleListView<Variation, ListitemHistoryExerciseRowBinding>
            exerciseList.unScrollableVertically = true
            exerciseList.init(item.variations, superSetExerciseListHandler)

            exerciseList.suppressLayout(true)
            binding.header.setOnClickListener { toggleBits(bitList, state) }

            superSetExerciseListHandler.setOnLongImageClickListener {
                context.goToVariation(it)
            }
        }

        bitList.visibility = if (state["expanded", false]) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    private fun bitClicked(bit: Bit, index: Int, bitList: MultipleListView<ITrainingRow>) {
        context.dbThread { db ->

            val enableInstantSwitch = db.bitDao().getPreviousByTraining(bit.trainingId, bit.timestamp)
                ?.let { it.variationId == bit.variation.id }
                ?: false

            context as Activity

            val editDialog = EditBitLogDialogFragment(
                R.string.title_registry,
                enableInstantSwitch,
                internationalSystem,
                bit,
                { editedBit ->
                    context.dbThread { db ->
                        db.bitDao().update(editedBit.toEntity())
                        context.runOnUiThread { bitList.notifyItemChanged(index) }
                        onBitChangedListener?.accept(editedBit)
                    }
                }
            )

            context.runOnUiThread {
                editDialog.show((context as FragmentActivity).supportFragmentManager, null)
            }
        }
    }

    private fun toggleBits(
        bitList: MultipleListView<ITrainingRow>,
        state: SimpleListView.ListElementState
    ) {
        val expanded = !state["expanded", false]
        bitList.visibility = if (expanded) View.VISIBLE else View.GONE
        state["expanded"] = expanded
    }

    fun expandAll(
        binding: ViewBinding?,
        item: ExerciseRows,
        state: SimpleListView.ListElementState
    ) {
        state["expanded"] = true

        binding?.let {
            val bitList = if (item.superSet == null)
                (it as ListitemHistoryExerciseHeaderBinding).bitList
            else
                (it as ListitemHistorySupersetExerciseHeaderBinding).bitList
            bitList.visibility = View.VISIBLE
        }
    }

    fun collapseAll(
        binding: ViewBinding?,
        item: ExerciseRows,
        state: SimpleListView.ListElementState
    ) {
        state["expanded"] = false

        binding?.let {
            val bitList = if (item.superSet == null)
                (it as ListitemHistoryExerciseHeaderBinding).bitList
            else
                (it as ListitemHistorySupersetExerciseHeaderBinding).bitList
            bitList.visibility = View.GONE
        }
    }
}