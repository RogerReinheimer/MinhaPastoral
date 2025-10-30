package com.example.anotacao

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

class Email_verificacao : AppCompatActivity() {

    private lateinit var txtVoltar: TextView
    private lateinit var imgVoltar: ImageView
    private lateinit var btnEnviar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email_verificacao)

        // ---------- FINDVIEWBYID ----------
        txtVoltar = findViewById(R.id.txtVoltar)
        imgVoltar = findViewById(R.id.imgVoltar)
        btnEnviar = findViewById(R.id.btnEnviar)

        // ---------- BOTÕES VOLTAR (200ms, 0.9) ----------
        txtVoltar.setOnClickListener {
            val scaleX = ObjectAnimator.ofFloat(txtVoltar, "scaleX", 1f, 0.9f, 1f)
            val scaleY = ObjectAnimator.ofFloat(txtVoltar, "scaleY", 1f, 0.9f, 1f)
            AnimatorSet().apply {
                playTogether(scaleX, scaleY)
                duration = 200
                start()
            }
            startActivity(Intent(this, Pag_esqueceu_senha::class.java))
        }

        imgVoltar.setOnClickListener {
            val scaleX = ObjectAnimator.ofFloat(imgVoltar, "scaleX", 1f, 0.9f, 1f)
            val scaleY = ObjectAnimator.ofFloat(imgVoltar, "scaleY", 1f, 0.9f, 1f)
            AnimatorSet().apply {
                playTogether(scaleX, scaleY)
                duration = 200
                start()
            }
            startActivity(Intent(this, Pag_esqueceu_senha::class.java))
        }

        // ---------- BOTÃO ENVIAR (300ms, 0.8) ----------
        btnEnviar.setOnClickListener {
            val scaleX = ObjectAnimator.ofFloat(btnEnviar, "scaleX", 1f, 0.8f, 1f)
            val scaleY = ObjectAnimator.ofFloat(btnEnviar, "scaleY", 1f, 0.8f, 1f)
            AnimatorSet().apply {
                playTogether(scaleX, scaleY)
                duration = 300
                start()
            }
            startActivity(Intent(this, Insira_nova_senha::class.java))
        }
    }
}
