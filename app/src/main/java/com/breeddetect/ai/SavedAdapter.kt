package com.breeddetect.ai

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class SavedAdapter(
    private var predictions: List<SavedPrediction>,
    private val onDeleteClick: (String) -> Unit
) : RecyclerView.Adapter<SavedAdapter.SavedViewHolder>() {

    class SavedViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivPrediction: ImageView = view.findViewById(R.id.ivPrediction)
        val tvBreed: TextView = view.findViewById(R.id.tvBreed)
        val tvConfidence: TextView = view.findViewById(R.id.tvConfidence)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_saved_prediction, parent, false)
        return SavedViewHolder(view)
    }

    override fun onBindViewHolder(holder: SavedViewHolder, position: Int) {
        val item = predictions[position]
        holder.tvBreed.text = item.breed.replaceFirstChar { it.uppercase() }
        holder.tvConfidence.text = "Confidence: ${item.confidence}%"
        
        val date = Date(item.timestamp)
        val format = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        holder.tvDate.text = format.format(date)

        // For now, we use a placeholder or logo since we didn't implement image upload to Storage
        holder.ivPrediction.setImageResource(R.drawable.boviscan_logo)

        holder.btnDelete.setOnClickListener {
            onDeleteClick(item.id)
        }
    }

    override fun getItemCount() = predictions.size

    fun updateData(newList: List<SavedPrediction>) {
        predictions = newList
        notifyDataSetChanged()
    }
}
