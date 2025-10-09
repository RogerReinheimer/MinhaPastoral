package com.example.anotacao

data class VerseResponse(
    val pk: Int,
    val translation: String,
    val book: Int,
    val chapter: Int,
    val verse: Int,
    val text: String
)