package org.scp.gymlog.ui.training

import android.app.Activity
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.scp.gymlog.R
import org.scp.gymlog.databinding.ListElementFragmentHistoryExerciseHeaderBinding
import org.scp.gymlog.databinding.ListElementFragmentHistorySupersetExerciseHeaderBinding
import org.scp.gymlog.exceptions.LoadException
import org.scp.gymlog.model.Bit
import org.scp.gymlog.model.Muscle
import org.scp.gymlog.room.DBThread
import org.scp.gymlog.ui.common.dialogs.EditBitLogDialogFragment
import org.scp.gymlog.ui.training.rows.TrainingBitRow
import java.io.IOException
import java.util.function.BiConsumer
import java.util.function.Consumer

class TrainingMainRecyclerViewAdapter(
    private val exerciseRows: List<ExerciseRows>,
    private val internationalSystem: Boolean,
    focusElement: Int
) : RecyclerView.Adapter<TrainingMainRecyclerViewAdapter.ViewHolder>() {

    private val holders: MutableList<ViewHolder> = ArrayList()
    private val expandedElements: MutableSet<Int> = HashSet()
    var onBitChangedListener: Consumer<Bit>? = null

    init {
        if (focusElement >= 0) {
            expandedElements.add(focusElement)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val row = exerciseRows[position]
        return if (row.superSet != null)
                R.layout.list_element_fragment_history_superset_exercise_header
            else
                R.layout.list_element_fragment_history_exercise_header
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when(viewType) {
            R.layout.list_element_fragment_history_superset_exercise_header -> ViewHolder(
                ListElementFragmentHistorySupersetExerciseHeaderBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
            else -> ViewHolder(
                ListElementFragmentHistoryExerciseHeaderBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holders.add(holder)
        val context = holder.itemView.context

        holder.exerciseRow = exerciseRows[position]
        val exerciseRow = holder.exerciseRow!!

        if (exerciseRow.superSet != null) {
            holder.mTitle?.text = exerciseRow.superSet.toString()

            val exercisesAdapter = TrainingVariationsRecyclerViewAdapter(exerciseRow)
            holder.mExerciseList?.adapter = exercisesAdapter
            holder.mExerciseList?.suppressLayout(true)
            holder.mExerciseList?.layoutManager = object : LinearLayoutManager(context) {
                override fun canScrollVertically(): Boolean {
                    return false
                }
            }

        } else {
            val exercise = exerciseRow.exercise

            holder.mTitle?.text = exercise.name
            holder.mSubtitle?.text = exercise.primaryMuscles
                .map(Muscle::text)
                .map { id: Int -> context.resources.getString(id) }
                .joinToString { it }

            holder.mIndicator?.setCardBackgroundColor(
                ResourcesCompat.getColor(context.resources, exercise.primaryMuscles[0].color, null))

            val fileName = "previews/" + exercise.image + ".png"
            try {
                val ims = context.assets.open(fileName)
                val d = Drawable.createFromStream(ims, null)
                holder.mImage?.setImageDrawable(d)

            } catch (e: IOException) {
                throw LoadException("Could not read \"$fileName\"", e)
            }
        }

        holder.mBitList.layoutManager = object : LinearLayoutManager(context) {
            override fun canScrollVertically(): Boolean {
                return false
            }
        }

        val adapter = TrainingRecyclerViewAdapter(exerciseRow, internationalSystem)
        holder.mBitList.adapter = adapter

        adapter.onClickListener = BiConsumer { bit: Bit?, index: Int ->
            val editDialog = EditBitLogDialogFragment(
                R.string.title_registry,
                exerciseRow[index - 1] is TrainingBitRow,
                internationalSystem, bit!!,
                { b: Bit -> DBThread.run(context) { db ->
                    db.bitDao().update(b.toEntity())
                    (context as Activity).runOnUiThread { adapter.notifyItemChanged(index) }
                    if (onBitChangedListener != null) {
                        onBitChangedListener!!.accept(b)
                    }
                }}
            )
            editDialog.show((context as FragmentActivity).supportFragmentManager, null)
        }

        holder.toggleBits(expandedElements.contains(position))
    }

    fun expandAll() {
        expandedElements.addAll(exerciseRows.indices)
        holders.forEach { it.toggleBits(true) }
    }

    fun collapseAll() {
        expandedElements.clear()
        holders.forEach { it.toggleBits(false) }
    }

    override fun getItemCount(): Int {
        return exerciseRows.size
    }




    inner class ViewHolder : RecyclerView.ViewHolder {

        var exerciseRow: ExerciseRows? = null
        val mTitle: TextView?
        val mSubtitle: TextView?
        val mIndicator: CardView?
        val mImage: ImageView?
        val mBitList: RecyclerView
        val mExerciseList: RecyclerView?

        constructor(binding: ListElementFragmentHistoryExerciseHeaderBinding) : super(binding.root) {
            mTitle = binding.row.title
            mSubtitle = binding.row.subtitle
            mIndicator = binding.row.indicator
            mImage = binding.row.image
            mBitList = binding.bitList
            mExerciseList = null

            binding.header.setOnClickListener { toggleBits() }
        }

        constructor(binding: ListElementFragmentHistorySupersetExerciseHeaderBinding) : super(binding.root) {
            mTitle = binding.superSetNumber
            mSubtitle = null
            mIndicator = null
            mImage = null
            mBitList = binding.bitList
            mExerciseList = binding.exerciseList

            binding.header.setOnClickListener { toggleBits() }
        }

        private fun toggleBits() {
            val index = exerciseRows.indexOf(exerciseRow)
            toggleBits(!expandedElements.contains(index))
        }

        fun toggleBits(show: Boolean) {
            mBitList.visibility = if (show) View.VISIBLE else View.GONE
            val index = exerciseRows.indexOf(exerciseRow)
            if (show) expandedElements.add(index) else expandedElements.remove(index)
        }

        override fun toString(): String {
            return super.toString() + " '" + mTitle?.text + "'"
        }
    }
}