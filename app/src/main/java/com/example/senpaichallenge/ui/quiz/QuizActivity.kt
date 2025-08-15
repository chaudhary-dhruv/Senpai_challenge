package com.example.senpaichallenge.ui.quiz

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.senpaichallenge.R
import com.example.senpaichallenge.models.Question
import com.example.senpaichallenge.models.Quiz
import com.example.senpaichallenge.adapters.OptionAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson

class QuizActivity : AppCompatActivity() {

    private lateinit var questionText: TextView
    private lateinit var btnPrev: Button
    private lateinit var btnNext: Button
    private lateinit var btnSubmit: Button

    private var animeName = ""
    private var index = 1

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var quiz: Quiz? = null
    private val questions: MutableMap<String, Question> = mutableMapOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        animeName = intent.getStringExtra("ANIME_NAME") ?: ""
        if (animeName.isEmpty()) {
            Toast.makeText(this, "No anime selected", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initViews()
        loadQuestionsFromFirestore()
    }

    private fun initViews() {
        questionText = findViewById(R.id.questionText)
        btnPrev = findViewById(R.id.btnPrevious)
        btnNext = findViewById(R.id.btnNext)
        btnSubmit = findViewById(R.id.btnSubmit)

        btnPrev.setOnClickListener {
            if (index > 1) {
                index--
                bindViews()
            }
        }
        btnNext.setOnClickListener {
            if (index < questions.size) {
                index++
                bindViews()
            }
        }
        btnSubmit.setOnClickListener {
            val intent = Intent(this, ScoreCardActivity::class.java)
            val json = Gson().toJson(quiz)
            intent.putExtra("QUIZ_DATA", json)
            startActivity(intent)
            finish()
        }
    }

    private fun loadQuestionsFromFirestore() {
        firestore.collection("quizzes").document(animeName)
            .get()
            .addOnSuccessListener { doc ->
                val questionsArray = doc.get("questions") as? List<Map<String, Any>>
                if (questionsArray.isNullOrEmpty()) {
                    Toast.makeText(this, "No questions found", Toast.LENGTH_SHORT).show()
                    finish()
                    return@addOnSuccessListener
                }

                var count = 1
                for (qData in questionsArray) {
                    val opts = qData["options"] as List<*>
                    val q = Question(
                        description = qData["text"].toString(),
                        option1 = opts[0].toString(),
                        option2 = opts[1].toString(),
                        option3 = opts[2].toString(),
                        option4 = opts[3].toString(),
                        correctIndex = (qData["correctIndex"] as Number).toInt()
                    )
                    questions["question$count"] = q
                    count++
                }

                quiz = Quiz(
                    id = animeName,
                    title = animeName,
                    questions = questions
                )
                bindViews()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load quiz", Toast.LENGTH_SHORT).show()
                finish()
            }
    }


    private fun bindViews() {
        btnPrev.visibility = View.GONE
        btnNext.visibility = View.GONE
        btnSubmit.visibility = View.GONE

        when (index) {
            1 -> btnNext.visibility = View.VISIBLE
            questions.size -> {
                btnPrev.visibility = View.VISIBLE
                btnSubmit.visibility = View.VISIBLE
            }
            else -> {
                btnPrev.visibility = View.VISIBLE
                btnNext.visibility = View.VISIBLE
            }
        }

        val question = questions["question$index"] ?: return
        questionText.text = question.description

        val rv = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.optionsRecyclerView)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = OptionAdapter(this, question)
    }
}
