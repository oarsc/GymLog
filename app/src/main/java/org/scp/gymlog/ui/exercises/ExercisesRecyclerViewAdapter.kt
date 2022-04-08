package org.scp.gymlog.ui.exercises

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
import org.scp.gymlog.exceptions.LoadException
import org.scp.gymlog.model.Exercise
import org.scp.gymlog.model.Order
import org.scp.gymlog.ui.common.dialogs.MenuDialogFragment
import org.scp.gymlog.util.Data
import org.scp.gymlog.util.DateUtils
import java.io.IOException
import java.util.*
import java.util.function.BiConsumer
import java.util.function.Consumer

class ExercisesRecyclerViewAdapter(
    exercisesList: List<Int>,
    private var order: Order,
    private val menuOptionCallback: BiConsumer<Exercise, Int>
) : RecyclerView.Adapter<ExercisesRecyclerViewAdapter.ViewHolder>() {

    private val exercises: MutableList<Exercise> = exercisesList
        .map { id -> Data.getExercise(id) } as MutableList<Exercise>
    private val orderedIndexes: MutableList<Int> = (0 until exercises.size).toMutableList()
    private val today: Calendar = Calendar.getInstance()
    var onClickListener: Consumer<Exercise>? = null

    init {
        updateOrder()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ListElementFragmentExerciseBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    fun removeExercise(ex: Exercise) {
        val idx = exercises.indexOf(ex)
        val orderedIdx = orderedIndexes.indexOf(idx)
        exercises.removeAt(idx)
        orderedIndexes.removeAt(orderedIdx)
        notifyItemRemoved(orderedIdx)
    }

    fun updateNotify(ex: Exercise) {
        val idx = exercises.indexOf(ex)
        val orderedIdx = orderedIndexes.indexOf(idx)
        notifyItemChanged(orderedIdx)
    }

    fun addExercise(ex: Exercise) {
        exercises.add(ex)
        val index = orderedIndexes.size
        orderedIndexes.add(index)
        notifyItemInserted(index)
        switchOrder(order)
    }

    fun switchOrder(order: Order) {
        this.order = order
        updateOrder()
        notifyItemRangeChanged(0, exercises.size)
    }

    private fun updateOrder() {
        val alphabeticalComparator = Comparator.comparing { i: Int ->
            exercises[i].name.lowercase(Locale.getDefault())
        }
        when (order) {
            Order.ALPHABETICALLY -> orderedIndexes.sortWith(alphabeticalComparator)
            Order.LAST_USED -> {
                val comparator = Comparator.comparing { i: Int -> exercises[i].lastTrained }
                orderedIndexes.sortWith(comparator.reversed().thenComparing(alphabeticalComparator))
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val context = holder.itemView.context

        holder.exercise = exercises[orderedIndexes[position]]
        holder.mContentView.text = holder.exercise!!.name

        val timeStr = DateUtils.calculateTimeLetter(holder.exercise!!.lastTrained, today)
        holder.mTime.visibility = if (timeStr.isEmpty()) View.GONE else View.VISIBLE
        holder.mTime.text = timeStr

        val fileName = "previews/" + holder.exercise!!.image + ".png"
        try {
            val ims = context.assets.open(fileName)
            val d = Drawable.createFromStream(ims, null)
            holder.mImageView.setImageDrawable(d)

        } catch (e: IOException) {
            throw LoadException("Could not read \"$fileName\"", e)
        }
    }

    override fun getItemCount(): Int {
        return exercises.size
    }

    inner class ViewHolder(binding: ListElementFragmentExerciseBinding) :
        RecyclerView.ViewHolder(binding.root) {

        var exercise: Exercise? = null
        val mImageView: ImageView = binding.image
        val mContentView: TextView = binding.content
        val mTime: TextView = binding.time

        init {
            itemView.setOnClickListener { onClickListener?.accept(exercise!!) }
            itemView.setOnLongClickListener {
                val dialog = MenuDialogFragment(R.menu.exercise_menu) { action ->
                    menuOptionCallback.accept(exercise!!, action)
                }
                val activity = binding.content.context as FragmentActivity
                dialog.show(activity.supportFragmentManager, null)
                true
            }
        }

        override fun toString(): String {
            return super.toString() + " '" + mContentView.text + "'"
        }
    }
}