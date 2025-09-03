package com.example.senpaichallenge.ui.quiz

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.senpaichallenge.MainActivity
import com.example.senpaichallenge.R
import com.example.senpaichallenge.adapters.OptionAdapter
import com.example.senpaichallenge.models.Question
import com.example.senpaichallenge.models.Quiz
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class QuizActivity : AppCompatActivity() {

    private lateinit var questionText: TextView
    private lateinit var questionCountText: TextView
    private lateinit var timerText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnNext: Button
    private lateinit var btnSubmit: Button

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var animeName = ""
    private var index = 1
    private var score = 0
    private var lastIndexFromDB = 0
    private var perQuestionAward = mutableMapOf<Int, Int>()

    private var quiz: Quiz? = null
    private val questions: MutableMap<String, Question> = mutableMapOf()

    private var timer: CountDownTimer? = null
    private val questionTime = 30_000L // 30s per question

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
        loadUserLastIndexThenQuestions()
    }

    private fun initViews() {
        questionText = findViewById(R.id.questionText)
        questionCountText = findViewById(R.id.tvQuestionCount)
        timerText = findViewById(R.id.timerText)
        progressBar = findViewById(R.id.progressBarTimer)
        btnNext = findViewById(R.id.btnNext)
        btnSubmit = findViewById(R.id.btnSubmit)

        //  Next button validation
        btnNext.setOnClickListener {
            val currentQuestion = questions["question$index"]
            if (currentQuestion?.userAnswerIndex == null) {
                score -= 100
            } else {
                if (index < questions.size) {
                    index++
                    bindViews()
                }
            }
        }
        btnSubmit.setOnClickListener {
            val currentQuestion = questions["question$index"]
            if (currentQuestion?.userAnswerIndex == null) {
                Toast.makeText(this, "Select the option", Toast.LENGTH_SHORT).show()
            } else {
                saveScoreToFirestore()
            }
        }
    }

    private fun loadUserLastIndexThenQuestions() {
        val uid = auth.currentUser?.uid ?: run {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show()
            finish(); return
        }
        firestore.collection("users").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                lastIndexFromDB = (doc.get("lastIndex.$animeName") as? Number)?.toInt() ?: 0
                loadQuestionsFromFirestore()
            }
            .addOnFailureListener {
                lastIndexFromDB = 0
                loadQuestionsFromFirestore()
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

                val sessionQuestions = questionsArray
                    .drop(lastIndexFromDB)
                    .take(10)

                if (sessionQuestions.isEmpty()) {
                    Toast.makeText(this, "No more questions for $animeName", Toast.LENGTH_SHORT).show()
                    finish()
                    return@addOnSuccessListener
                }

                var count = 1
                for (qData in sessionQuestions) {
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

                quiz = Quiz(id = animeName, title = animeName, questions = questions)
                bindViews()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load quiz", Toast.LENGTH_SHORT).show()
                finish()
            }
    }

    private fun bindViews() {
        timer?.cancel()

        btnNext.visibility = View.GONE
        btnSubmit.visibility = View.GONE

        when (index) {
            questions.size -> btnSubmit.visibility = View.VISIBLE
            else -> btnNext.visibility = View.VISIBLE
        }

        val question = questions["question$index"] ?: return
        questionText.text = question.description
        questionCountText.text = "Question $index/${questions.size}"

        val rv = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.optionsRecyclerView)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = OptionAdapter(this, question) { selected ->
            // scoring logic: Correct = +100, Wrong = -25
            val gained = if (selected == question.correctIndex) 100 else -25
            val prev = perQuestionAward[index] ?: 0
            score += (gained - prev)
            perQuestionAward[index] = gained
        }

        startTimer()
    }

    private fun startTimer() {
        progressBar.max = (questionTime / 1000).toInt()
        progressBar.progress = progressBar.max

        timer = object : CountDownTimer(questionTime, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = (millisUntilFinished / 1000).toInt()
                timerText.text = "Time: ${seconds}s"
                progressBar.progress = seconds
            }

            override fun onFinish() {
                val currentQuestion = questions["question$index"]
                if (currentQuestion?.userAnswerIndex == null) {
                    //  Time finished & no option selected → -50
                    val prev = perQuestionAward[index] ?: 0
                    score += (-50 - prev)
                    perQuestionAward[index] = -50
                }

                if (index < questions.size) {
                    index++
                    bindViews()
                } else {
                    saveScoreToFirestore()
                }
            }
        }.start()
    }

    private var quizFinished = false

    private fun saveScoreToFirestore() {
        timer?.cancel()
        quizFinished = true
        val uid = auth.currentUser?.uid ?: run {
            goToScoreCard()
            return
        }

        val answeredCount = questions.size
        val userDoc = firestore.collection("users").document(uid)
        firestore.runTransaction { tx ->
            val snap = tx.get(userDoc)
            val currentTotal = snap.getLong("totalPoints") ?: 0
            val animePts = (snap.get("animePoints.$animeName") as? Long) ?: 0
            val lastIdx = (snap.get("lastIndex.$animeName") as? Long) ?: 0

            tx.update(userDoc, mapOf(
                "totalPoints" to (currentTotal + score),
                "animePoints.$animeName" to (animePts + score),
                "lastIndex.$animeName" to (lastIdx + answeredCount)
            ))
        }.addOnCompleteListener {
            goToScoreCard()
        }
    }

    private fun goToScoreCard() {
        quizFinished = true   // ✅ Mark as finished
        val intent = Intent(this, ScoreCardActivity::class.java)
        intent.putExtra("SCORE", score)
        intent.putExtra("TOTAL", questions.size * 100)
        intent.putExtra("ANIME_NAME", animeName)
        startActivity(intent)
        finish()
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (!quizFinished) {   // Only trigger when quiz is not finished
            Toast.makeText(this, "You left the quiz. Returning to home.", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("OPEN_FRAGMENT", "HOME")
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }




    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }


    // immersive fullscreen mode
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            window.decorView.systemUiVisibility =
                (View.SYSTEM_UI_FLAG_IMMERSIVE
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
        }
    }
}
