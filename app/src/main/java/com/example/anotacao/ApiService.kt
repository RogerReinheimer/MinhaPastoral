package com.example.anotacao

import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("get-random-verse/{version}/")
    suspend fun getVersiculoAleatorio(
        @Path("version") version: String
    ): List<ApiResponse>
}