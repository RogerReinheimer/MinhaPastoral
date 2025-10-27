package com.example.anotacao

import android.content.Intent
import android.os.Bundle
import android.os.Handler // 1. IMPORTAR O HANDLER
import android.os.Looper  // 2. IMPORTAR O LOOPER
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Instala a splash (mantém o que fizemos, para evitar a tela branca)
        installSplashScreen()

        super.onCreate(savedInstanceState)

        // Define o seu layout XML para ser a tela visível
        setContentView(R.layout.tela_carregamento)

        // 3. CRIA O ATRASO DE 2 SEGUNDOS
        Handler(Looper.getMainLooper()).postDelayed({

            // --- O CÓDIGO ABAIXO SÓ VAI EXECUTAR APÓS 2 SEGUNDOS ---

            // 4. Verifica o estado de login
            val firebaseAuth = FirebaseAuth.getInstance()
            val currentUser = firebaseAuth.currentUser

            if (currentUser != null) {
                // Usuário está LOGADO
                // Envia para a tela principal (ex: MainAnotacao)
                val intent = Intent(this, Pag_home::class.java)
                startActivity(intent)
            } else {
                // Usuário está DESLOGADO
                // Envia para a tela de login (MainActivity)
                val intent = Intent(this, Pag_entrar::class.java)
                startActivity(intent)
            }

            // 5. Finaliza a SplashActivity
            // Isso impede que o usuário possa "voltar" para a tela de carregamento
            finish()

        }, 2000) // 2000 = 2000 milissegundos (ou 2 segundos). Mude se quiser.
    }
}