package com.example.senpaichallenge.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.senpaichallenge.R
import com.example.senpaichallenge.models.Question

class OptionAdapter(
    private val context: Context,
    private val question: Question,
    private val onSelected: (Int) -> Unit
) : RecyclerView.Adapter<OptionAdapter.OptionViewHolder>() {

    private val options = listOf(
        question.option1, question.option2, question.option3, question.option4
    )

    private var selectedPosition = question.userAnswerIndex ?: -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OptionViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.option_item, parent, false)
        return OptionViewHolder(view)
    }

    override fun onBindViewHolder(holder: OptionViewHolder, position: Int) {
        holder.optionView.text = options[position]

        // Highlight based on selected option
        if (selectedPosition == position) {
            holder.optionView.setBackgroundResource(R.drawable.option_item_selected_bg)
            holder.optionView.setTextColor(ContextCompat.getColor(context, android.R.color.black))
        } else {
            holder.optionView.setBackgroundResource(R.drawable.option_item_bg)
            holder.optionView.setTextColor(ContextCompat.getColor(context, android.R.color.white))
        }

        // Click listener with safe adapterPosition
        holder.itemView.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                selectedPosition = pos
                question.userAnswerIndex = pos
                onSelected(pos)
                notifyDataSetChanged()
            }
        }
    }

    override fun getItemCount(): Int = options.size

    inner class OptionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val optionView: TextView = itemView.findViewById(R.id.quiz_option)
    }
}
