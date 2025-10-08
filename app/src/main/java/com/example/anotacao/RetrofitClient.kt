package com.example.anotacao

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "https://bolls.life/api/"

    // --- INÍCIO DA NOVA CONFIGURAÇÃO DE INSPEÇÃO ---

    // 1. Cria o inspetor
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // BODY mostra todos os detalhes da requisição
    }

    // 2. Cria o cliente de rede e adiciona o inspetor a ele
    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    // --- FIM DA NOVA CONFIGURAÇÃO DE INSPEÇÃO ---


    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client) // 3. Diz ao Retrofit para usar nosso cliente com o inspetor
            .build()
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}