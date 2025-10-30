package com.example.anotacao

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()

    companion object {
        private const val SPLASH_DELAY = 2000L // 2 segundos
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        installSplashScreen()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.tela_carregamento)

        Handler(Looper.getMainLooper()).postDelayed({
            verificarEstadoLoginENavegar()
        }, SPLASH_DELAY)
    }

    private fun verificarEstadoLoginENavegar() {
        val currentUser = auth.currentUser

        val intent = if (currentUser != null) {
            Intent(this, Pag_home::class.java)
        } else {
            Intent(this, Pag_entrar::class.java)
        }

        startActivity(intent)
        finish()
    }
}
