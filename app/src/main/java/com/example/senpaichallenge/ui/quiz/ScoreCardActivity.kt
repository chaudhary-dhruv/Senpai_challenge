package com.example.senpaichallenge.ui.quiz

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.senpaichallenge.MainActivity
import com.example.senpaichallenge.R

class ScoreCardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_score_card)

        val score = intent.getIntExtra("SCORE", 0)
        val total = intent.getIntExtra("TOTAL", 0)

        val percentage = if (total > 0) (score * 100 / total) else 0

        val progressCircle = findViewById<ProgressBar>(R.id.progressCircle)
        val tvPercentage = findViewById<TextView>(R.id.tvPercentage)
        val tvScore = findViewById<TextView>(R.id.tvScore)
        val btnGoHome = findViewById<Button>(R.id.btnGoHome)

        // Animate circular progress
        ObjectAnimator.ofInt(progressCircle, "progress", 0, percentage).apply {
            duration = 1500 // 1.5s animation
            start()
        }

        // Animate percentage text
        Thread {
            for (i in 0..percentage) {
                runOnUiThread {
                    tvPercentage.text = "$i%"
                }
                Thread.sleep(50) // smooth increment
            }
        }.start()

        // Set values
        progressCircle.progress = percentage
        tvPercentage.text = "$percentage%"
        tvScore.text = "Your score in this quiz: \nscore: $score/$total"

        btnGoHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("OPEN_FRAGMENT", "HOME")
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
}
