package org.oar.gymlog.ui.training

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.viewbinding.ViewBinding
import org.oar.gymlog.R
import org.oar.gymlog.databinding.ListitemHistoryExerciseHeaderBinding
import org.oar.gymlog.model.Bit
import org.oar.gymlog.ui.common.components.listView.CommonListView
import org.oar.gymlog.ui.common.components.listView.MultipleListView
import org.oar.gymlog.ui.common.components.listView.SimpleListHandler
import org.oar.gymlog.ui.common.dialogs.EditBitLogDialogFragment
import org.oar.gymlog.ui.training.rows.ITrainingBitRow
import org.oar.gymlog.ui.training.rows.TrainingBitHeaderRow
import org.oar.gymlog.ui.training.rows.TrainingBitRow
import org.oar.gymlog.ui.training.rows.TrainingRowData
import org.oar.gymlog.util.extensions.DatabaseExts.dbThread
import org.oar.gymlog.util.extensions.RedirectionExts.goToVariation
import java.util.function.Consumer

class TrainingListSuperSetHandler(
    val context: Context,
    private val internationalSystem: Boolean,
    private val showTotals: Boolean,
) : SimpleListHandler<TrainingRowData, ListitemHistoryExerciseHeaderBinding> {
    override val useListState = false
    override val itemInflater: (LayoutInflater, ViewGroup?, Boolean) -> ListitemHistoryExerciseHeaderBinding
        = ListitemHistoryExerciseHeaderBinding::inflate

    private var onBitChangedListener: Consumer<Bit>? = null

    fun setOnBitChangedListener(onBitChangedListener: Consumer<Bit>) {
        this.onBitChangedListener = onBitChangedListener
    }

    @Suppress("UNCHECKED_CAST")
    override fun buildListView(
        binding: ListitemHistoryExerciseHeaderBinding,
        item: TrainingRowData,
        index: Int,
        state: CommonListView.ListElementState?
    ) {
        val bitList = binding.bitList as MultipleListView<ITrainingBitRow>
        bitList.unScrollableVertically = true

        val bitHandler = TrainingBitListHandler(internationalSystem, showTotals)
        val rows: List<ITrainingBitRow> = generateBitRows(item)
        bitList.init(rows, bitHandler)
        bitHandler.setOnClickListener { bit, idx ->
            bitClicked(bit, idx, bitList)
        }

        val variation = item.variation
        val exercise = variation.exercise

        binding.row.exerciseName.text = exercise.name
        if (variation.default) {
            binding.row.variationName.visibility = View.GONE
        } else {
            binding.row.variationName.visibility = View.VISIBLE
            binding.row.variationName.text = variation.name
        }

        binding.row.image.setImage(exercise.image, exercise.primaryMuscles[0].color)

        binding.row.root.setOnClickListener { toggleBits(bitList, item) }
        binding.row.root.setOnLongClickListener {
            context.goToVariation(variation)
            true
        }

        bitList.visibility = if (item.expanded) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    private fun generateBitRows(rowData: TrainingRowData): List<ITrainingBitRow> {
        val rows = mutableListOf<ITrainingBitRow>(
            TrainingBitHeaderRow(true)
        )

        rowData
            .map { TrainingBitRow(it) }
            .let { rows.addAll(it) }

        return rows
    }

    private fun bitClicked(bit: Bit, index: Int, bitList: MultipleListView<ITrainingBitRow>) {
        context.dbThread { db ->

            val enableInstantSwitch = db.bitDao().getPreviousByTraining(bit.trainingId, bit.timestamp)
                ?.let { it.variationId == bit.variation.id }
                ?: false

            context as Activity

            val editDialog = EditBitLogDialogFragment(
                title = R.string.title_registry,
                enableInstantSwitch = enableInstantSwitch,
                internationalSystem = internationalSystem,
                initialValue = bit,
                confirmListener = { editedBit, cloned ->
                    context.runOnUiThread {
                        if (cloned) {
                            bitList.insert(index + 1, TrainingBitRow(editedBit))
                            onBitChangedListener?.accept(editedBit)
                        } else {
                            bitList.notifyItemChanged(index)
                            onBitChangedListener?.accept(editedBit)
                        }
                    }
                }
            )

            context.runOnUiThread {
                editDialog.show((context as FragmentActivity).supportFragmentManager, null)
            }
        }
    }

    private fun toggleBits(
        bitList: MultipleListView<ITrainingBitRow>,
        item: TrainingRowData
    ) {
        val expanded = !item.expanded
        bitList.visibility = if (expanded) View.VISIBLE else View.GONE
        item.expanded = expanded
    }

    fun expandItem(
        binding: ViewBinding?,
        item: TrainingRowData
    ) {
        item.expanded = true
        binding?.let {
            val bitList = (it as ListitemHistoryExerciseHeaderBinding).bitList
            bitList.visibility = View.VISIBLE
        }
    }

    fun collapseItem(
        binding: ViewBinding?,
        item: TrainingRowData
    ) {
        item.expanded = false
        binding?.let {
            val bitList = (it as ListitemHistoryExerciseHeaderBinding).bitList
            bitList.visibility = View.GONE
        }
    }
}