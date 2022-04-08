package org.scp.gymlog.ui.main.routines

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.scp.gymlog.databinding.ListElementFragmentMuscleBinding
import org.scp.gymlog.ui.main.routines.placeholder.PlaceholderContent

/**
 * [RecyclerView.Adapter] that can display a [PlaceholderItem].
 * TODO: Replace the implementation with code for your data type.
 */
class RoutineRecyclerViewAdapter : RecyclerView.Adapter<RoutineRecyclerViewAdapter.ViewHolder>() {

	private val mValues: List<PlaceholderContent.PlaceholderItem>

	init {
		mValues = PlaceholderContent.ITEMS
	}
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		return ViewHolder(
			ListElementFragmentMuscleBinding.inflate(
				LayoutInflater.from(parent.context), parent, false
			)
		)
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		holder.mItem = mValues[position]
		holder.mContentView.text = mValues[position].content
	}

	override fun getItemCount(): Int {
		return mValues.size
	}

	inner class ViewHolder(
		binding: ListElementFragmentMuscleBinding
	) : RecyclerView.ViewHolder(binding.root) {

		val mContentView: TextView = binding.content
		var mItem: PlaceholderContent.PlaceholderItem? = null

		override fun toString(): String {
			return super.toString() + " '" + mContentView.text + "'"
		}
	}
}
