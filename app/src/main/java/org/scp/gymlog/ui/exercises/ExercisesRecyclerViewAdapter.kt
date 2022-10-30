package org.scp.gymlog.ui.exercises

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import org.scp.gymlog.R
import org.scp.gymlog.databinding.ListElementFragmentExerciseBinding
import org.scp.gymlog.databinding.ListElementFragmentVariationBinding
import org.scp.gymlog.exceptions.LoadException
import org.scp.gymlog.model.Exercise
import org.scp.gymlog.model.Order
import org.scp.gymlog.model.Variation
import org.scp.gymlog.ui.common.dialogs.MenuDialogFragment
import org.scp.gymlog.util.Data
import org.scp.gymlog.util.DateUtils.getLetterFrom
import org.scp.gymlog.util.DateUtils.currentDateTime
import java.io.IOException
import java.time.LocalDateTime
import java.util.*
import java.util.function.BiConsumer
import java.util.function.Consumer

class ExercisesRecyclerViewAdapter(
    exercisesList: List<Int>,
    private var order: Order,
    private val menuOptionCallback: BiConsumer<Variation, Int>
) : RecyclerView.Adapter<ExercisesRecyclerViewAdapter.ViewHolder>() {

    private val variations: MutableList<ExpandableExercisesModel> = exercisesList
        .map { id -> Data.getExercise(id) }
        .flatMap { it.variations }
        .filter { it.default }
        .map { ExpandableExercisesModel(it) }
        .toMutableList()

    private val orderedIndexes: MutableList<Int> = (0 until variations.size).toMutableList()
    private val today: LocalDateTime = currentDateTime()
    var onClickListener: Consumer<Variation>? = null

    init {
        updateOrder()
    }

    override fun getItemViewType(position: Int): Int {
        return if (variations[orderedIndexes[position]].isChild)
            R.layout.list_element_fragment_variation
        else
            R.layout.list_element_fragment_exercise
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when(viewType){
            R.layout.list_element_fragment_exercise -> ViewHolder(
                ListElementFragmentExerciseBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
            else -> ViewHolder(
                ListElementFragmentVariationBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }
    }

    fun removeExercise(exercise: Exercise) {
        val indexes = variations
            .filter { it.exercise == exercise }
            .map { variations.indexOf(it) }
            .map { orderedIndexes.indexOf(it) }

        val min = indexes.minOf { it }
        val size = indexes.size

        val originalTotalSize = orderedIndexes.size
        variations.removeIf { it.exercise == exercise }
        indexes.sorted().reversed().forEach { orderedIndexes.removeAt(it) }

        var substract = 0
        (0 until originalTotalSize).forEach {
            val idx = orderedIndexes.indexOf(it)
            if (idx < 0) {
                substract++
            } else {
                orderedIndexes[idx] -= substract
            }
        }

        notifyItemRangeRemoved(min, size)
    }

    fun updateNotify(exercise: Exercise) {
        val indexes = variations
            .filter { it.exercise == exercise }
            .map { variations.indexOf(it) }
            .map { orderedIndexes.indexOf(it) }

        exercise.variations
            .filter { !it.default }
            .forEach { variationChanged ->
                val index = variations.map { it.variation.id }.indexOf(variationChanged.id)
                if (index >= 0) {
                    val oldModel = variations[index]
                    variations[index] = ExpandableExercisesModel(variationChanged, oldModel.parent)
                }
            }

        val min = indexes.minOf { it }
        val size = indexes.size
        notifyItemRangeChanged(min, size)
    }

    fun addExercise(exercise: Exercise) {
        exercise.variations
            .map { ExpandableExercisesModel(it) }
            .also { variations.addAll(it) }

        val index = orderedIndexes.size
        val added = exercise.variations.size

        repeat(exercise.variations.size) {
            orderedIndexes.add(orderedIndexes.size)
        }

        notifyItemRangeInserted(index, added)
        switchOrder(order)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun switchOrder(order: Order) {
        this.order = order
        updateOrder()
        notifyDataSetChanged()
    }

    private fun updateOrder() {
        val alphabeticalComparator = Comparator.comparing { i: Int ->
            variations[i].exercise.name.lowercase(Locale.getDefault())
        }
        when (order) {
            Order.ALPHABETICALLY -> orderedIndexes.sortWith(alphabeticalComparator)
            Order.LAST_USED -> {
                val comparator = Comparator.comparing { i: Int -> variations[i].exercise.lastTrained }
                orderedIndexes.sortWith(comparator.reversed().thenComparing(alphabeticalComparator))
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val context = holder.itemView.context

        holder.model = variations[orderedIndexes[position]]
        val variation = holder.model.variation

        if (!holder.model.isChild) {
            holder.mContentView.text = variation.exercise.name

            val timeStr = today.getLetterFrom(variation.exercise.lastTrained)
            holder.mTime?.visibility = if (timeStr.isEmpty()) View.GONE else View.VISIBLE
            holder.mTime?.text = timeStr

            val fileName = "previews/" + variation.exercise.image + ".png"
            try {
                val ims = context.assets.open(fileName)
                val d = Drawable.createFromStream(ims, null)
                holder.mImageView?.setImageDrawable(d)

            } catch (e: IOException) {
                throw LoadException("Could not read \"$fileName\"", e)
            }
        } else {
            holder.mContentView.text = variation.name
        }
    }

    override fun getItemCount(): Int {
        return variations.size
    }

    fun expand(model: ExpandableExercisesModel) {
        if (!model.expanded) {
            model.expanded = true

            val newModels = model.exercise.variations
                //.filter { !it.default }
                .map { ExpandableExercisesModel(it, model) }
                .also { variations.addAll(it) }

            val last = variations.size
            val first = last - newModels.size

            val currentIndex = variations.indexOf(model)
            val orderedIndex = orderedIndexes.indexOf(currentIndex)

            (first until last).reversed().forEach {
                orderedIndexes.add(orderedIndex+1, it)
            }

            notifyItemRangeInserted(orderedIndex+1, newModels.size)
        }
    }

    fun collapse(model: ExpandableExercisesModel) {
        val parent = model.parent ?: model
        if (parent.expanded) {
            parent.expanded = false

            val newModels = variations
                .filter { it.parent === parent }

            val first = variations.indexOf(newModels[0])
            val last = first + newModels.size

            val orderedIndex = orderedIndexes.indexOf(first)

            variations.removeIf { it.parent === parent }
            orderedIndexes.removeIf { it in first until last }
            orderedIndexes.forEachIndexed { index, it ->
                if (it >= last)
                    orderedIndexes[index] = it - newModels.size
            }

            notifyItemRangeRemoved(orderedIndex, newModels.size)
        }
    }

    inner class ViewHolder : RecyclerView.ViewHolder {

        lateinit var model: ExpandableExercisesModel
        val root: View
        val mImageView: ImageView?
        val mContentView: TextView
        val mTime: TextView?

        constructor(binding: ListElementFragmentExerciseBinding) : super(binding.root) {
            root = binding.root
            mImageView = binding.image
            mContentView = binding.content
            mTime = binding.time

            itemView.setOnClickListener {
                if (model.canExpand) {
                    if (model.expanded)
                        collapse(model)
                    else
                        expand(model)

                } else {
                    onClickListener?.accept(model.variation)
                }
            }
            itemView.setOnLongClickListener {
                val dialog = MenuDialogFragment(R.menu.exercise_menu) { action ->
                    menuOptionCallback.accept(model.variation, action)
                }
                val activity = binding.content.context as FragmentActivity
                dialog.show(activity.supportFragmentManager, null)
                true
            }
        }

        constructor(binding: ListElementFragmentVariationBinding) : super(binding.root) {
            root = binding.root
            mImageView = null
            mContentView = binding.content
            mTime = null

            itemView.setOnClickListener {
                onClickListener?.accept(model.variation)
            }
        }

        override fun toString(): String {
            return super.toString() + " '" + mContentView.text + "'"
        }
    }
}