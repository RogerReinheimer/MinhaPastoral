package com.example.anotacao.ui.login

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.anotacao.R
// V IMPORTS NOVOS DO FIREBASE V
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class Pag_Cadastro : AppCompatActivity() {

    // 1. DECLARANDO A REFERÊNCIA PARA O FIREBASE AUTHENTICATION
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_cadastro)

        // 2. INICIALIZANDO O FIREBASE AUTH
        auth = Firebase.auth

        // --- Lógica para Voltar (mantida) ---
        val txtVoltar = findViewById<TextView>(R.id.txtVoltar)
        txtVoltar.setOnClickListener { finish() }
        val imgVoltar = findViewById<ImageView>(R.id.imgVoltar)
        imgVoltar.setOnClickListener { finish() }

        // --- Referências dos campos ---
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etUsuario = findViewById<EditText>(R.id.etUsuario)
        val etSenha = findViewById<EditText>(R.id.etSenha)
        val etConfirmaSenha = findViewById<EditText>(R.id.etConfirmaSenha)
        val btnCadastrar = findViewById<Button>(R.id.btnCadastrar)

        // 3. LÓGICA DO BOTÃO DE CADASTRO (MODIFICADA PARA FIREBASE)
        btnCadastrar.setOnClickListener {
            val email = etEmail.text.toString()
            val usuario = etUsuario.text.toString()
            val senha = etSenha.text.toString()
            val confirmaSenha = etConfirmaSenha.text.toString()

            if (email.isNotEmpty() && usuario.isNotEmpty() && senha.isNotEmpty()) {
                if (senha == confirmaSenha) {
                    // Chama a nova função de cadastro com Firebase
                    cadastrarUsuarioFirebase(email, usuario, senha)
                } else {
                    Toast.makeText(this, "As senhas não conferem!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 4. NOVA FUNÇÃO PARA CADASTRAR COM FIREBASE
    private fun cadastrarUsuarioFirebase(email: String, usuario: String, senha: String) {
        // O Firebase já faz a verificação de e-mail e senha (mínimo 6 caracteres)
        auth.createUserWithEmailAndPassword(email, senha)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sucesso ao criar o usuário no Authentication!
                    // Agora, vamos salvar o nome de usuário no Firestore.
                    val firebaseUser = auth.currentUser
                    val uid = firebaseUser?.uid // ID único do usuário gerado pelo Firebase

                    if (uid != null) {
                        salvarDadosAdicionaisUsuario(uid, usuario, email)
                    }
                } else {
                    // Falha ao criar o usuário. O Firebase nos dá a mensagem de erro.
                    val exception = task.exception
                    Toast.makeText(baseContext, "Erro: ${exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    // 5. NOVA FUNÇÃO PARA SALVAR DADOS EXTRAS NO FIRESTORE
    private fun salvarDadosAdicionaisUsuario(uid: String, username: String, email: String) {
        val db = Firebase.firestore
        val dadosUsuario = hashMapOf(
            "username" to username,
            "email" to email
            // Você pode adicionar mais campos aqui no futuro
        )

        // Criamos um novo "documento" na coleção "users" usando o UID como chave
        db.collection("users").document(uid)
            .set(dadosUsuario)
            .addOnSuccessListener {
                // Sucesso em todo o processo!
                Toast.makeText(this, "Usuário cadastrado com sucesso!", Toast.LENGTH_SHORT).show()
                finish() // Fecha a tela de cadastro e volta para o login
            }
            .addOnFailureListener { e ->
                // Se falhar aqui, o usuário foi criado no Auth, mas sem os dados extras.
                // É importante tratar este caso em um app de produção.
                Toast.makeText(this, "Erro ao salvar informações adicionais: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}