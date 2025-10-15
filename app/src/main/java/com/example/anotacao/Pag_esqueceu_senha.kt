package com.example.anotacao

import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class Pag_esqueceu_senha : AppCompatActivity() {

    // 1. Declaração das variáveis para os componentes da UI e para o Firebase Auth
    private lateinit var auth: FirebaseAuth
    private lateinit var etEmail: EditText
    private lateinit var btnEnviar: Button
    private lateinit var txtVoltar: TextView
    private lateinit var imgVoltar: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pag_esqueceu_senha)


        auth = Firebase.auth

        // 3. Conexão das variáveis com os componentes do XML pelos seus IDs
        etEmail = findViewById(R.id.etEmailNS)
        btnEnviar = findViewById(R.id.btnEnviar)
        txtVoltar = findViewById(R.id.txtVoltarParaLogin)
        imgVoltar = findViewById(R.id.imgVoltarLogin)


        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Ação para o botão "Enviar"
        btnEnviar.setOnClickListener {
            val email = etEmail.text.toString().trim()
            enviarLinkDeRedefinicao(email)
        }


        txtVoltar.setOnClickListener {
            finish()
        }

        imgVoltar.setOnClickListener {
            finish()
        }
    }

    private fun enviarLinkDeRedefinicao(email: String) {
        // 5. Validação do campo de e-mail
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

        // Desativa o botão para evitar múltiplos cliques enquanto a operação ocorre
        btnEnviar.isEnabled = false

        // 6. Chamada da função do Firebase para enviar o e-mail
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