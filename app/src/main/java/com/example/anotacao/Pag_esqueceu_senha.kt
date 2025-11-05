package com.example.anotacao

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth

class Pag_esqueceu_senha : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var etEmail: EditText
    private lateinit var btnEnviar: Button
    private lateinit var txtVoltar: TextView
    private lateinit var imgVoltar: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pag_esqueceu_senha)

        auth = FirebaseAuth.getInstance()

        // ---------- FINDVIEWBYID ----------
        etEmail = findViewById(R.id.etEmailNS)
        btnEnviar = findViewById(R.id.btnEnviar)
        txtVoltar = findViewById(R.id.txtVoltarParaLogin)
        imgVoltar = findViewById(R.id.imgVoltarLogin)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        // ---------- BOTÃO ENVIAR (200ms, 0.9) ----------
        btnEnviar.setOnClickListener {
            val scaleX = ObjectAnimator.ofFloat(btnEnviar, "scaleX", 1f, 0.9f, 1f)
            val scaleY = ObjectAnimator.ofFloat(btnEnviar, "scaleY", 1f, 0.9f, 1f)
            AnimatorSet().apply {
                playTogether(scaleX, scaleY)
                duration = 200
                start()
            }

            val email = etEmail.text.toString().trim()
            enviarLinkDeRedefinicao(email)
        }

        // ---------- BOTÃO VOLTAR TEXTO (200ms, 0.9) ----------
        txtVoltar.setOnClickListener {
            val scaleX = ObjectAnimator.ofFloat(txtVoltar, "scaleX", 1f, 0.9f, 1f)
            val scaleY = ObjectAnimator.ofFloat(txtVoltar, "scaleY", 1f, 0.9f, 1f)
            AnimatorSet().apply {
                playTogether(scaleX, scaleY)
                duration = 200
                start()
            }
            finish()
        }

        // ---------- BOTÃO VOLTAR IMAGEM (200ms, 0.9) ----------
        imgVoltar.setOnClickListener {
            val scaleX = ObjectAnimator.ofFloat(imgVoltar, "scaleX", 1f, 0.9f, 1f)
            val scaleY = ObjectAnimator.ofFloat(imgVoltar, "scaleY", 1f, 0.9f, 1f)
            AnimatorSet().apply {
                playTogether(scaleX, scaleY)
                duration = 200
                start()
            }
            finish()
        }
    }

    private fun enviarLinkDeRedefinicao(email: String) {
        if (email.isEmpty()) {
            etEmail.error = "O campo de e-mail é obrigatório."
            etEmail.requestFocus()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Por favor, insira um e-mail válido."
            etEmail.requestFocus()
            return
        }

        btnEnviar.isEnabled = false

        // Envio simples - Firebase automaticamente usará o intent-filter do AndroidManifest
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        this,
                        "E-mail de redefinição enviado com sucesso! Verifique sua caixa de entrada.",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                } else {
                    Toast.makeText(
                        this,
                        "Falha ao enviar e-mail: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }

                btnEnviar.isEnabled = true
            }
    }
}
