package com.example.anotacao

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.FirebaseAuth

class Insira_nova_senha : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var etSenhaNova: EditText
    private lateinit var etConfirmaSenha: EditText
    private lateinit var btnEnviar: Button
    private lateinit var txtVoltar: TextView
    private lateinit var imgVoltar: ImageView

    private var actionCode: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_insira_nova_senha)

        auth = FirebaseAuth.getInstance()

        // ---------- FINDVIEWBYID ----------
        etSenhaNova = findViewById(R.id.etSenhaNova)
        etConfirmaSenha = findViewById(R.id.etConfirmaSenha)
        btnEnviar = findViewById(R.id.btnEnviar)
        txtVoltar = findViewById(R.id.txtVoltar)
        imgVoltar = findViewById(R.id.imgVoltar)

        handleIntent()
        setupClickListeners()
    }

    private fun handleIntent() {
        actionCode = intent.data?.getQueryParameter("oobCode")

        if (actionCode == null) {
            Toast.makeText(this, "Link inválido ou expirado. Tente o processo novamente.", Toast.LENGTH_LONG).show()
            finish()
        }
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
            handleConfirmarNovaSenha()
        }

        // ---------- BOTÕES VOLTAR (200ms, 0.9) ----------
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

    private fun handleConfirmarNovaSenha() {
        val novaSenha = etSenhaNova.text.toString()
        val confirmaSenha = etConfirmaSenha.text.toString()

        if (novaSenha.isEmpty() || confirmaSenha.isEmpty()) {
            Toast.makeText(this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show()
            return
        }

        if (novaSenha.length < 6) {
            etSenhaNova.error = "A senha deve ter no mínimo 6 caracteres."
            return
        }

        if (novaSenha != confirmaSenha) {
            etConfirmaSenha.error = "As senhas não são iguais."
            return
        }

        val code = actionCode ?: return

        btnEnviar.isEnabled = false

        auth.confirmPasswordReset(code, novaSenha)
            .addOnCompleteListener { task ->
                btnEnviar.isEnabled = true

                if (task.isSuccessful) {
                    Toast.makeText(this, "Senha alterada com sucesso!", Toast.LENGTH_LONG).show()

                    val intent = Intent(this, Pag_entrar::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(
                        this,
                        "Erro: O link pode ter expirado. Por favor, solicite um novo.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
}
