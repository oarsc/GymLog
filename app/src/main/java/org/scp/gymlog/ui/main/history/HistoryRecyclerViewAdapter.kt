package org.scp.gymlog.ui.main.history

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.scp.gymlog.R
import org.scp.gymlog.databinding.ListitemTrainingBinding
import org.scp.gymlog.model.Muscle
import org.scp.gymlog.ui.training.TrainingActivity
import org.scp.gymlog.util.DateUtils.getTimeString
import org.scp.gymlog.util.DateUtils.minutesToTimeString

class HistoryRecyclerViewAdapter : RecyclerView.Adapter<HistoryRecyclerViewAdapter.ViewHolder>() {

	private val trainingDataList = mutableListOf<TrainingData>()
	private lateinit var ctx: Context

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		ctx = parent.context
		return ViewHolder(
			ListitemTrainingBinding.inflate(
				LayoutInflater.from(ctx), parent, false
			)
		)
	}

	@SuppressLint("SetTextI18n")
	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		val trainingData = trainingDataList[position]

		holder.id = trainingData.id
		holder.mTitle.text = if (trainingData.duration == null)
				String.format(
					ctx.getString(R.string.compound_training_start_date),
					trainingData.id,
					trainingData.startTime.getTimeString()
				)
			else
				String.format(
					ctx.getString(R.string.compound_training_duration),
					trainingData.id,
					trainingData.duration?.minutesToTimeString()
				)

		holder.mSubtitle.text = trainingData.mostUsedMuscles
			.map(Muscle::text)
			.map { resText -> ctx.resources.getString(resText) }
			.joinToString { it }

		holder.mIndicator.setBackgroundResource(trainingData.mostUsedMuscles[0].color)
	}

	fun clear() {
		trainingDataList.clear()
	}

	fun size(): Int {
		return trainingDataList.size
	}

	fun add(trainingData: TrainingData) {
		trainingDataList.add(trainingData)
	}

	@SuppressLint("NotifyDataSetChanged")
	fun notifyItemsChanged(initialSize: Int, endSize: Int) {
		if (initialSize == 0) {
			if (endSize != 0) {
				notifyItemInserted(endSize)
			}
		} else if (endSize == 0) {
			notifyItemRangeRemoved(0, initialSize)
		} else if (initialSize == endSize) {
			notifyItemRangeChanged(0, initialSize)
		} else {
			notifyDataSetChanged()
		}
	}

	override fun getItemCount(): Int {
		return trainingDataList.size
	}

	inner class ViewHolder(binding: ListitemTrainingBinding) :
		RecyclerView.ViewHolder(binding.root) {

		var id = 0
		val mTitle: TextView = binding.title
		val mSubtitle: TextView = binding.subtitle
		val mIndicator: View = binding.indicator

		init {
			itemView.setOnClickListener {
				val intent = Intent(ctx, TrainingActivity::class.java)
				intent.putExtra("trainingId", id)
				ctx.startActivity(intent)
			}
		}

		override fun toString(): String {
			return super.toString() + " '" + mTitle.text + "'"
		}
	}
}