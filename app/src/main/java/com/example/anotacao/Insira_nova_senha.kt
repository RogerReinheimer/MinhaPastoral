package com.example.anotacao

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.util.Log
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
    private lateinit var btnMostrarSenha: ImageView
    private lateinit var btnMostrarConfirmaSenha: ImageView
    private lateinit var btnEnviar: Button
    private lateinit var txtVoltar: TextView
    private lateinit var imgVoltar: ImageView

    private var actionCode: String? = null
    private var senhaVisivel = false
    private var confirmaSenhaVisivel = false

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_insira_nova_senha)

        auth = FirebaseAuth.getInstance()

        // ---------- FINDVIEWBYID ----------
        etSenhaNova = findViewById(R.id.etSenhaNova)
        etConfirmaSenha = findViewById(R.id.etConfirmaSenha)
        btnMostrarSenha = findViewById(R.id.btnMostrarSenha)
        btnMostrarConfirmaSenha = findViewById(R.id.btnMostrarConfirmaSenha)
        btnEnviar = findViewById(R.id.btnEnviar)
        txtVoltar = findViewById(R.id.txtVoltar)
        imgVoltar = findViewById(R.id.imgVoltar)

        processarDeepLink()
        setupClickListeners()
    }

    private fun processarDeepLink() {
        // Obter o oobCode da URL do link do e-mail
        actionCode = intent.data?.getQueryParameter("oobCode")

        Log.d("Insira_nova_senha", "Código recebido: $actionCode")

        if (actionCode == null) {
            Toast.makeText(
                this,
                "Link inválido ou expirado. Tente o processo novamente.",
                Toast.LENGTH_LONG
            ).show()
            finish()
        }
    }

    private fun setupClickListeners() {
        // ---------- BOTÃO MOSTRAR SENHA ----------
        btnMostrarSenha.setOnClickListener {
            val scaleX = ObjectAnimator.ofFloat(btnMostrarSenha, "scaleX", 1f, 0.9f, 1f)
            val scaleY = ObjectAnimator.ofFloat(btnMostrarSenha, "scaleY", 1f, 0.9f, 1f)
            AnimatorSet().apply {
                playTogether(scaleX, scaleY)
                duration = 200
                start()
            }

            senhaVisivel = !senhaVisivel
            if (senhaVisivel) {
                etSenhaNova.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                btnMostrarSenha.setImageResource(R.drawable.ic_visibility)
            } else {
                etSenhaNova.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                btnMostrarSenha.setImageResource(R.drawable.ic_visibility_off)
            }
            etSenhaNova.setSelection(etSenhaNova.text.length)
        }

        // ---------- BOTÃO MOSTRAR CONFIRMAR SENHA ----------
        btnMostrarConfirmaSenha.setOnClickListener {
            val scaleX = ObjectAnimator.ofFloat(btnMostrarConfirmaSenha, "scaleX", 1f, 0.9f, 1f)
            val scaleY = ObjectAnimator.ofFloat(btnMostrarConfirmaSenha, "scaleY", 1f, 0.9f, 1f)
            AnimatorSet().apply {
                playTogether(scaleX, scaleY)
                duration = 200
                start()
            }

            confirmaSenhaVisivel = !confirmaSenhaVisivel
            if (confirmaSenhaVisivel) {
                etConfirmaSenha.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                btnMostrarConfirmaSenha.setImageResource(R.drawable.ic_visibility)
            } else {
                etConfirmaSenha.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                btnMostrarConfirmaSenha.setImageResource(R.drawable.ic_visibility_off)
            }
            etConfirmaSenha.setSelection(etConfirmaSenha.text.length)
        }

        // ---------- BOTÃO ENVIAR ----------
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

        // ---------- BOTÕES VOLTAR ----------
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
