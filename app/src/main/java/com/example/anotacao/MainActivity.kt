package com.example.anotacao

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.anotacao.ui.login.Pag_Cadastro
// V IMPORTS NOVOS DO FIREBASE V
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    // 1. DECLARANDO A REFERÊNCIA PARA O FIREBASE AUTHENTICATION
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pag_entrar)

        // 2. INICIALIZANDO O FIREBASE AUTH
        auth = Firebase.auth

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // --- Sua lógica de navegação para outras telas ---
        val txtCadastrar = findViewById<TextView>(R.id.txtCadastrar)
        txtCadastrar.setOnClickListener {
            val intent = Intent(this, Pag_Cadastro::class.java)
            startActivity(intent)
        }
        val txtEsqueceuSenha = findViewById<TextView>(R.id.txtEsqueceuSenha)
        txtEsqueceuSenha.setOnClickListener {
            val intent = Intent(this, Pag_esqueceu_senha::class.java)
            startActivity(intent)
        }

        // 3. PEGANDO AS REFERÊNCIAS DOS CAMPOS DE LOGIN
        val etEmail = findViewById<EditText>(R.id.etEmailUsuario)
        val etSenha = findViewById<EditText>(R.id.etSenha)
        val btnEntrar = findViewById<Button>(R.id.btnEntrar)

        // 4. LÓGICA DO BOTÃO ENTRAR COM FIREBASE
        btnEntrar.setOnClickListener {
            val emailDigitado = etEmail.text.toString()
            val senhaDigitada = etSenha.text.toString()

            if (emailDigitado.isNotEmpty() && senhaDigitada.isNotEmpty()) {
                realizarLoginFirebase(emailDigitado, senhaDigitada)
            } else {
                Toast.makeText(this, "Por favor, preencha o e-mail e a senha.", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    // 5. NOVA FUNÇÃO PARA VERIFICAR O LOGIN COM FIREBASE
    private fun realizarLoginFirebase(email: String, senha: String) {
        auth.signInWithEmailAndPassword(email, senha)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // SUCESSO!
                    Toast.makeText(this, "Login bem-sucedido!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, Pag_home::class.java)
                    startActivity(intent)
                    finish() // Fecha a tela de login
                }
            }
    }
}