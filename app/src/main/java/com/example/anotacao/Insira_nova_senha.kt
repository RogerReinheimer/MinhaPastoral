package com.example.anotacao

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

// Renomeie a classe para o nome que você deu à sua Activity
class Insira_nova_senha : AppCompatActivity() {

    // 1. Declaração das variáveis
    private lateinit var auth: FirebaseAuth
    private lateinit var etSenhaNova: EditText
    private lateinit var etConfirmaSenha: EditText
    private lateinit var btnEnviar: Button // AppCompatButton é um tipo de Button
    private lateinit var txtVoltar: TextView
    private lateinit var imgVoltar: ImageView

    // Variável para guardar o código que vem no link do e-mail
    private var actionCode: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Certifique-se de que o nome do seu arquivo XML está correto aqui
        setContentView(R.layout.activity_insira_nova_senha)

        auth = Firebase.auth

        // 2. Conectando as variáveis com os componentes do XML pelos seus IDs
        etSenhaNova = findViewById(R.id.etSenhaNova)
        etConfirmaSenha = findViewById(R.id.etConfirmaSenha)
        btnEnviar = findViewById(R.id.btnEnviar)
        txtVoltar = findViewById(R.id.txtVoltar)
        imgVoltar = findViewById(R.id.imgVoltar)

        // 3. Captura o código de ação do link que abriu esta activity
        handleIntent()

        // 4. Configura os cliques dos botões
        setupClickListeners()
    }

    private fun handleIntent() {
        actionCode = intent.data?.getQueryParameter("oobCode")

        // Se a activity foi aberta sem um código válido no link, ela é inútil.
        if (actionCode == null) {
            Toast.makeText(this, "Link inválido ou expirado. Tente o processo novamente.", Toast.LENGTH_LONG).show()
            finish() // Fecha a activity
        }
    }

    private fun setupClickListeners() {
        btnEnviar.setOnClickListener {
            handleConfirmarNovaSenha()
        }

        val goBack = { finish() }
        txtVoltar.setOnClickListener { goBack() }
        imgVoltar.setOnClickListener { goBack() }
    }

    private fun handleConfirmarNovaSenha() {
        val novaSenha = etSenhaNova.text.toString()
        val confirmaSenha = etConfirmaSenha.text.toString()

        // --- 5. Validações ---
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

        val code = actionCode ?: return // Garante que o código não é nulo

        btnEnviar.isEnabled = false // Desativa o botão para evitar cliques duplos

        // --- 6. Lógica do Firebase ---
        auth.confirmPasswordReset(code, novaSenha)
            .addOnCompleteListener { task ->
                btnEnviar.isEnabled = true // Reativa o botão

                if (task.isSuccessful) {
                    Toast.makeText(this, "Senha alterada com sucesso!", Toast.LENGTH_LONG).show()

                    // Envia o usuário para a tela de login, limpando as telas anteriores
                    // Certifique-se de que 'LoginActivity::class.java' aponta para sua tela de login correta
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