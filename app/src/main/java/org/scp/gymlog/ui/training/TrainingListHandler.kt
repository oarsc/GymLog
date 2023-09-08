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
import org.scp.gymlog.ui.common.CustomAppCompatActivity
import org.scp.gymlog.ui.common.components.listView.CommonListView
import org.scp.gymlog.ui.common.components.listView.MultipleListHandler
import org.scp.gymlog.ui.common.components.listView.MultipleListView
import org.scp.gymlog.ui.common.components.listView.SimpleListView
import org.scp.gymlog.ui.common.dialogs.EditBitLogDialogFragment
import org.scp.gymlog.ui.common.dialogs.TextSelectDialogFragment
import org.scp.gymlog.ui.training.rows.ITrainingBitRow
import org.scp.gymlog.ui.training.rows.TrainingBitHeaderRow
import org.scp.gymlog.ui.training.rows.TrainingBitRow
import org.scp.gymlog.ui.training.rows.TrainingRowData
import org.scp.gymlog.util.extensions.DatabaseExts.dbThread
import org.scp.gymlog.util.extensions.RedirectionExts.goToVariation
import java.io.IOException
import java.util.function.Consumer

class TrainingListHandler(
    val context: Context,
    private val internationalSystem: Boolean,
    var preExpandedBitId: Int? = null,
) : MultipleListHandler<TrainingRowData> {
    override val useListState = false
    override val itemInflaters = listOf<(LayoutInflater, ViewGroup?, Boolean) -> ViewBinding>(
        ListitemHistoryExerciseHeaderBinding::inflate,
        ListitemHistorySupersetContainerBinding::inflate
    )

    override fun findItemInflaterIndex(item: TrainingRowData) = item.superSet?.let { 1 } ?: 0

    private var onBitChangedListener: Consumer<Bit>? = null

    fun setOnBitChangedListener(onBitChangedListener: Consumer<Bit>) {
        this.onBitChangedListener = onBitChangedListener
    }

    @Suppress("UNCHECKED_CAST")
    override fun buildListView(
        binding: ViewBinding,
        item: TrainingRowData,
        index: Int,
        state: CommonListView.ListElementState?
    ) {
        if (item.superSet == null) {
            binding as ListitemHistoryExerciseHeaderBinding

            preExpandedBitId?.let { bitId ->
                if (item.any { it.id == bitId }) {
                    item.expanded = true
                    preExpandedBitId = null
                }
            }

            val bitList = binding.bitList as MultipleListView<ITrainingBitRow>
            bitList.unScrollableVertically = true

            val bitHandler = TrainingBitListHandler(internationalSystem)
            val rows: List<ITrainingBitRow> = generateBitRows(item)
            bitList.init(rows, bitHandler)
            bitHandler.setOnClickListener { bit, idx ->
                bitClicked(bit, idx, bitList)
            }

            val variation = item.variation
            val exercise = variation.exercise

            binding.row.title.text = exercise.name
            if (variation.default) {
                binding.row.subtitle.setText(R.string.text_default)
            } else {
                binding.row.subtitle.text = variation.name
            }

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

            binding.header.setOnClickListener { toggleBits(bitList, item) }

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

            bitList.visibility = if (item.expanded) {
                View.VISIBLE
            } else {
                View.GONE
            }

        } else {
            binding as ListitemHistorySupersetContainerBinding

            val bitList = binding.exerciseList as SimpleListView<TrainingRowData, ListitemHistoryExerciseHeaderBinding>
            bitList.unScrollableVertically = true

            val handler = TrainingListSuperSetHandler(context, internationalSystem)
            handler.setOnBitChangedListener { onBitChangedListener?.accept(it) }

            val subRows = splitSuperSetRows(item).onEach { it.expanded = item.expanded }

            preExpandedBitId?.let { bitId ->
                subRows.forEach { row ->
                    if (row.any { it.id == bitId }) {
                        row.expanded = true
                        preExpandedBitId = null
                    }
                }
            }

            bitList.init(subRows, handler)

            binding.superSetNumber.text = item.superSet.toString()
        }
    }

    private fun generateBitRows(rowData: TrainingRowData): List<ITrainingBitRow> {
        val rows = mutableListOf<ITrainingBitRow>(
            TrainingBitHeaderRow()
        )

        rowData
            .map { TrainingBitRow(it) }
            .let { rows.addAll(it) }

        return rows
    }

    private fun splitSuperSetRows(rowData: TrainingRowData): List<TrainingRowData> {
        val rows = mutableListOf<TrainingRowData>()

        rowData.forEach { bit ->
            val variation = bit.variation

            val row = rows
                .filter { it.variation === variation }
                .getOrElse(0) {
                    TrainingRowData(variation, rowData.superSet).also { rows.add(it) }
                }
            row.add(bit)
        }
        return rows
    }

    private fun bitClicked(bit: Bit, index: Int, bitList: MultipleListView<ITrainingBitRow>) {
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
            if (item.superSet == null) {
                it as ListitemHistoryExerciseHeaderBinding

                val bitList = it.bitList
                bitList.visibility = View.VISIBLE

            } else {
                it as ListitemHistorySupersetContainerBinding

                val bitList = it.exerciseList as SimpleListView<TrainingRowData, ListitemHistoryExerciseHeaderBinding>
                val trainingListHandler = bitList.handler as TrainingListSuperSetHandler

                bitList.applyToAll { subBinding, item, _ ->
                    trainingListHandler.expandItem(subBinding, item)
                }
            }
        }
    }

    fun collapseItem(
        binding: ViewBinding?,
        item: TrainingRowData
    ) {
        item.expanded = false

        binding?.let {
            if (item.superSet == null) {
                val bitList = (it as ListitemHistoryExerciseHeaderBinding).bitList
                bitList.visibility = View.GONE

            } else {
                it as ListitemHistorySupersetContainerBinding

                val bitList = it.exerciseList as SimpleListView<TrainingRowData, ListitemHistoryExerciseHeaderBinding>
                val trainingListHandler = bitList.handler as TrainingListSuperSetHandler

                bitList.applyToAll { subBinding, item, _ ->
                    trainingListHandler.collapseItem(subBinding, item)
                }
            }
        }
    }
}