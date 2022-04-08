package org.scp.gymlog.ui.main.muscles

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.scp.gymlog.databinding.ListElementFragmentMuscleBinding
import org.scp.gymlog.model.Muscle
import org.scp.gymlog.util.Data
import java.util.function.Consumer

class MusclesRecyclerViewAdapter(
	private val onClickElementListener: Consumer<Muscle>
) : RecyclerView.Adapter<MusclesRecyclerViewAdapter.ViewHolder>() {

	private val muscles: List<Muscle>

	init {
		muscles = Data.muscles
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		return ViewHolder(
			ListElementFragmentMuscleBinding.inflate(
				LayoutInflater.from(parent.context), parent, false
			)
		)
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		val context = holder.itemView.context
		val muscle = muscles[position].also { holder.muscle = it }

		holder.mContentView.setText(muscle.text)
		holder.mImageView.setImageResource(muscle.icon)
		holder.mImageView.imageTintList = ColorStateList.valueOf(
			context.resources.getColor(muscle.color, null)
		)
		holder.mIndicator.setBackgroundResource(muscle.color)
	}

	override fun getItemCount(): Int {
		return muscles.size
	}

	inner class ViewHolder(binding: ListElementFragmentMuscleBinding) :
		RecyclerView.ViewHolder(binding.root) {

		var muscle: Muscle? = null
		val mContentView: TextView = binding.content
		val mImageView: ImageView = binding.image
		val mIndicator: View = binding.indicator

		init {
			itemView.setOnClickListener { onClickElementListener.accept(muscle!!) }
		}

		override fun toString(): String {
			return super.toString() + " '" + mContentView.text + "'"
		}
	}
}
