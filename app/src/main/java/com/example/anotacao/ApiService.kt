package com.example.anotacao

import retrofit2.http.GET

interface BibleApi {
    @GET("get-random-verse/NVT/")
    suspend fun getRandomVerse(): VerseResponse
}
