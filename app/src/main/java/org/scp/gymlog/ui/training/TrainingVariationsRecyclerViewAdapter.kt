package org.scp.gymlog.ui.training

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import org.scp.gymlog.R
import org.scp.gymlog.databinding.ListitemHistoryExerciseRowBinding
import org.scp.gymlog.exceptions.LoadException
import java.io.IOException

class TrainingVariationsRecyclerViewAdapter(
    private val exerciseRow: ExerciseRows,
) : RecyclerView.Adapter<TrainingVariationsRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ListitemHistoryExerciseRowBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val context = holder.itemView.context

        val variation = exerciseRow.variations[position]
        val exercise = variation.exercise

        holder.mTitle.text = exercise.name
        if (variation.default) {
            holder.mSubtitle.setText(R.string.text_default)
        } else {
            holder.mSubtitle.text = variation.name
        }

        holder.mIndicator.setCardBackgroundColor(
            ResourcesCompat.getColor(context.resources, exercise.primaryMuscles[0].color, null))

        holder.mCardText.text = (position + 1).toString()

        val fileName = "previews/" + exercise.image + ".png"
        try {
            val ims = context.assets.open(fileName)
            val d = Drawable.createFromStream(ims, null)
            holder.mImageView?.setImageDrawable(d)

        } catch (e: IOException) {
            throw LoadException("Could not read \"$fileName\"", e)
        }

    }

    override fun getItemCount(): Int {
        return exerciseRow.variations.size
    }

    inner class ViewHolder(binding: ListitemHistoryExerciseRowBinding) : RecyclerView.ViewHolder(binding.root) {

        val root: View = binding.root
        val mImageView: ImageView = binding.image
        val mTitle: TextView = binding.title
        val mSubtitle: TextView = binding.subtitle
        val mIndicator: CardView = binding.indicator
        val mCardText: TextView = binding.cardText

        override fun toString(): String {
            return super.toString() + " '" + mTitle.text + "'"
        }
    }
}