package com.example.senpaichallenge.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.senpaichallenge.R
import com.example.senpaichallenge.models.Question

class OptionAdapter(
    private val context: Context,
    private val question: Question) :
    RecyclerView.Adapter<OptionAdapter.OptionViewHolder>() {

    private var selectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OptionViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.option_item, parent, false)
        return OptionViewHolder(view)
    }

    override fun onBindViewHolder(holder: OptionViewHolder, position: Int) {
        val options= listOf(
            question.option1, question.option2, question.option3, question.option4
        )

        holder.optionView.text = options[position]

        if (selectedPosition == position) {
            holder.optionView.setBackgroundResource(R.drawable.option_item_selected_bg)
        } else {
            holder.optionView.setBackgroundResource(R.drawable.option_item_bg)
        }

        holder.itemView.setOnClickListener {
            question.userAnswerIndex = position
            notifyDataSetChanged()
        }

    }

    inner class OptionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val optionView: TextView = itemView.findViewById(R.id.quiz_option)
    }

    override fun getItemCount(): Int = 4
}
