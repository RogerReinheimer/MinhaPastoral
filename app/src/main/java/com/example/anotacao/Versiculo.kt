package com.example.anotacao

data class ApiResponse(
    val reference: Reference,
    val text: String
)

data class Reference(
    val book: Book,
    val chapter: Int,
    val verse: Int
)

data class Book(
    val name: String,
    val testament: String
)