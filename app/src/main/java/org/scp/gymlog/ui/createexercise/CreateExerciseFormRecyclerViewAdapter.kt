package org.scp.gymlog.ui.createexercise

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.scp.gymlog.databinding.ListElementFragmentFormBinding

class CreateExerciseFormRecyclerViewAdapter(
	private val formElements: List<FormElement>
) :	RecyclerView.Adapter<CreateExerciseFormRecyclerViewAdapter.ViewHolder>() {

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		return ViewHolder(
			ListElementFragmentFormBinding.inflate(
				LayoutInflater.from(parent.context), parent, false
			)
		)
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		val element = formElements[position];

		holder.formElement = element
		holder.mTitleView.setText(element.title)
		holder.updateValue()
		element.updateListener = Runnable { holder.updateValue() }
	}

	override fun getItemCount(): Int {
		return formElements.size
	}

	inner class ViewHolder(binding: ListElementFragmentFormBinding) :
		RecyclerView.ViewHolder(binding.root) {
		var formElement: FormElement? = null
		val mTitleView: TextView = binding.title
		private val mContentView: TextView = binding.content
		private val mImageView: ImageView = binding.image

		init {
			itemView.setOnClickListener { view -> formElement?.onClick(view) }
		}

		fun updateValue() {
			mImageView.setImageDrawable(formElement!!.drawable)

			if (formElement!!.valueStr.isNotEmpty())
				mContentView.text = formElement!!.valueStr
			else if (formElement!!.value == 0)
				mContentView.text = "-"
			else
				mContentView.setText(formElement!!.value)
		}

		override fun toString(): String {
			return super.toString() + " '" + mContentView.text + "'"
		}
	}
}