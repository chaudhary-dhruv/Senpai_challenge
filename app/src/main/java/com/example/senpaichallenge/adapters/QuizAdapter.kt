package com.example.senpaichallenge.adapters

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.senpaichallenge.R
import com.example.senpaichallenge.ui.quiz.QuizActivity

data class QuizItem(
    val animeName: String,
    val iconResId: Int = R.drawable.ic_launcher_foreground
)

class QuizAdapter(
    private val context: Context,
    private val quizList: List<QuizItem>
) : RecyclerView.Adapter<QuizAdapter.QuizViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuizViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.quiz_item, parent, false)
        return QuizViewHolder(view)
    }

    override fun onBindViewHolder(holder: QuizViewHolder, position: Int) {
        val quiz = quizList[position]
        holder.quizTitle.text = quiz.animeName
        holder.quizIcon.setImageResource(quiz.iconResId)

        val colors = listOf("#FF5722", "#4CAF50", "#3F51B5", "#9C27B0", "#03A9F4")
        holder.cardContainer.setCardBackgroundColor(Color.parseColor(colors[position % colors.size]))

        holder.itemView.setOnClickListener {
            val intent = Intent(context, QuizActivity::class.java)
            intent.putExtra("ANIME_NAME", quiz.animeName)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = quizList.size

    inner class QuizViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val quizTitle: TextView = itemView.findViewById(R.id.quizTitle)
        val quizIcon: ImageView = itemView.findViewById(R.id.quizIcon)
        val cardContainer: CardView = itemView.findViewById(R.id.cardContainer)
    }
}
