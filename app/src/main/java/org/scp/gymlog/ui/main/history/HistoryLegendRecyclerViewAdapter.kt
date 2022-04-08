package org.scp.gymlog.ui.main.history

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import org.scp.gymlog.databinding.ListElementFragmentLegendBinding
import org.scp.gymlog.model.Muscle
import org.scp.gymlog.util.Data

class HistoryLegendRecyclerViewAdapter :
	RecyclerView.Adapter<HistoryLegendRecyclerViewAdapter.ViewHolder>() {

	private val muscles: MutableList<Muscle>
	private var size: Int
	private var context: Context? = null
	private var showingAll = false

	init {
		muscles = ArrayList(Data.muscles)
		size = muscles.size
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		context = parent.context
		return ViewHolder(
			ListElementFragmentLegendBinding.inflate(
				LayoutInflater.from(context), parent, false
			)
		)
	}

	@SuppressLint("NotifyDataSetChanged")
	fun focusMuscles(focusedMuscles: List<Muscle?>?) {
		if (focusedMuscles == null || focusedMuscles.isEmpty()) {
			if (!showingAll) {
				showingAll = true
				size = muscles.size
				muscles.sortWith(Comparator.comparing(Muscle::id))
				notifyDataSetChanged()
			}

		} else {
			val initSize = itemCount
			showingAll = false
			size = focusedMuscles.size

			muscles.sortWith { m1: Muscle, m2: Muscle ->
				if (focusedMuscles.contains(m1)) {
					if (focusedMuscles.contains(m2))
						focusedMuscles.indexOf(m2).compareTo(focusedMuscles.indexOf(m1))
					else
						-1

				} else {
					if (focusedMuscles.contains(m2))
						1
					else
						m1.id.compareTo(m2.id)
				}
			}
			if (size == initSize) {
				notifyItemRangeChanged(0, size)
			} else {
				notifyDataSetChanged()
			}
		}
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		val muscle = muscles[position]
		holder.mText.setText(muscle.text)
		holder.mIndicator.setCardBackgroundColor(
			ResourcesCompat.getColor(context!!.resources, muscle.color, null)
		)
	}

	override fun getItemCount(): Int {
		return size
	}

	inner class ViewHolder(binding: ListElementFragmentLegendBinding) :
		RecyclerView.ViewHolder(binding.root) {

		var muscle: Muscle? = null
		val mText: TextView = binding.text
		val mIndicator: CardView = binding.indicator

		override fun toString(): String {
			return super.toString() + " '" + mText.text + "'"
		}
	}
}
