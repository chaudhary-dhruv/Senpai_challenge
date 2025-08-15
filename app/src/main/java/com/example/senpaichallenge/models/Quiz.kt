package com.example.senpaichallenge.models

data class Quiz(
    var id: String = "",
    var title: String = "",
    var questions: Map<String, Question> = mapOf()
)
