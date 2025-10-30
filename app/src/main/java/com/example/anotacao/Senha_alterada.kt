package com.example.anotacao

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

class Senha_alterada : AppCompatActivity() {

    private lateinit var btnVoltar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_senha_alterada)

        // ---------- FINDVIEWBYID ----------
        btnVoltar = findViewById(R.id.btnVoltar)

        // ---------- BOTÃO VOLTAR (300ms, 0.8) ----------
        btnVoltar.setOnClickListener {
            val scaleX = ObjectAnimator.ofFloat(btnVoltar, "scaleX", 1f, 0.8f, 1f)
            val scaleY = ObjectAnimator.ofFloat(btnVoltar, "scaleY", 1f, 0.8f, 1f)
            AnimatorSet().apply {
                playTogether(scaleX, scaleY)
                duration = 300
                start()
            }
            startActivity(Intent(this, Pag_entrar::class.java))
        }
    }
}
