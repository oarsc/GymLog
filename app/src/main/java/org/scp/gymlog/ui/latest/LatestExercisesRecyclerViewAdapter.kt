package org.scp.gymlog.ui.latest

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.scp.gymlog.databinding.ListitemVariationBinding
import org.scp.gymlog.exceptions.LoadException
import org.scp.gymlog.model.Variation
import java.io.IOException
import java.util.function.Consumer

class LatestExercisesRecyclerViewAdapter(
    val variations: List<Variation>
) : RecyclerView.Adapter<LatestExercisesRecyclerViewAdapter.ViewHolder>() {

    var onClickListener: Consumer<Variation>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ListitemVariationBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val context = holder.itemView.context

        holder.model = variations[position]
        val variation = holder.model

        holder.mExerciseName.text = variation.exercise.name
        holder.mVariationName.apply {
            if (variation.default) visibility = View.GONE
            else                   text = variation.name
        }

        val fileName = "previews/" + variation.exercise.image + ".png"
        try {
            val ims = context.assets.open(fileName)
            val d = Drawable.createFromStream(ims, null)
            holder.mImageView?.setImageDrawable(d)

        } catch (e: IOException) {
            throw LoadException("Could not read \"$fileName\"", e)
        }
    }

    override fun getItemCount(): Int {
        return variations.size
    }

    inner class ViewHolder(binding: ListitemVariationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        lateinit var model: Variation
        val root: View
        val mImageView: ImageView?
        val mExerciseName: TextView
        val mVariationName: TextView

        init {
            root = binding.root
            mImageView = binding.image
            mExerciseName = binding.exerciseName
            mVariationName = binding.variationName
            binding.time.visibility = View.GONE

            itemView.setOnClickListener {
                onClickListener?.accept(model)
            }
        }

        override fun toString(): String {
            return super.toString() + " '${mExerciseName.text} - ${mVariationName.text}'"
        }
    }
}