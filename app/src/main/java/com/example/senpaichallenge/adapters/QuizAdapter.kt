package com.example.senpaichallenge.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.senpaichallenge.R
import com.example.senpaichallenge.ui.quiz.QuizActivity

data class QuizItem(
    val animeName: String
)

class QuizAdapter(
    private val context: Context,
    private val quizList: List<QuizItem>
) : RecyclerView.Adapter<QuizAdapter.QuizViewHolder>() {

    // Map animeName -> drawable
    private val imageMap = mapOf(
        "naruto" to R.drawable.naruto_banner,
        "one_piece" to R.drawable.onepiece_banner,
        "bleach" to R.drawable.bleach_banner,
        "demon_slayer" to R.drawable.demonslayer_banner,
        "attack_on_titan" to R.drawable.aot_banner,
        "boruto" to R.drawable.boruto_banner,
        "hunter_x_hunter" to R.drawable.hunterxhunter_banner,
        "dragon_ball_z" to R.drawable.dragonball_banner,
        "jujutsu_kaisen" to R.drawable.jjk_banner,
        "death_note" to R.drawable.deathnote_banner,
        "your_name" to R.drawable.yourname_banner,
        "my_hero_academia" to R.drawable.myheroacademia_banner,
        "one_punch_man" to R.drawable.onepunchman_banner,
        "spirited_away" to R.drawable.spiritedaway_banner,
        "akira" to R.drawable.akira_banner
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuizViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.quiz_item, parent, false)
        return QuizViewHolder(view)
    }

    override fun onBindViewHolder(holder: QuizViewHolder, position: Int) {
        val quiz = quizList[position]
        val resId = imageMap[quiz.animeName] ?: R.drawable.default_anime
        holder.imageView.setImageResource(resId)

        // 🔹 Show custom dialog before starting quiz
        holder.itemView.setOnClickListener {
            val dialogView = LayoutInflater.from(context).inflate(R.layout.custom_quiz_dialog, null)

            val alertDialog = androidx.appcompat.app.AlertDialog.Builder(context)
                .setView(dialogView)
                .create()

            // Set message dynamically with anime name
            val tvMessage = dialogView.findViewById<TextView>(R.id.tvDialogMessage)
            tvMessage.text = "Get ready! Do you want to start the '${quiz.animeName}' quiz?"

            // Buttons
            val btnStart = dialogView.findViewById<Button>(R.id.btnStart)
            val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)

            btnStart.setOnClickListener {
                val intent = Intent(context, QuizActivity::class.java)
                intent.putExtra("ANIME_NAME", quiz.animeName)
                context.startActivity(intent)
                alertDialog.dismiss()
            }

            btnCancel.setOnClickListener {
                alertDialog.dismiss()
            }

            alertDialog.show()
        }
    }

    override fun getItemCount(): Int = quizList.size

    inner class QuizViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imgAnimeCard)
    }
}
