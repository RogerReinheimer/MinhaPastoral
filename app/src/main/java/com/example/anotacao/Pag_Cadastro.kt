package com.example.anotacao.ui.login

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.anotacao.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Pag_Cadastro : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var btnCadastrar: Button
    private lateinit var txtVoltar: TextView
    private lateinit var imgVoltar: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_cadastro)

        auth = FirebaseAuth.getInstance()

        // ---------- FINDVIEWBYID ----------
        txtVoltar = findViewById(R.id.txtVoltar)
        imgVoltar = findViewById(R.id.imgVoltar)
        btnCadastrar = findViewById(R.id.btnCadastrar)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etUsuario = findViewById<EditText>(R.id.etUsuario)
        val etSenha = findViewById<EditText>(R.id.etSenha)
        val etConfirmaSenha = findViewById<EditText>(R.id.etConfirmaSenha)

        // ---------- BOTÃO VOLTAR (200ms, 0.9) ----------
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

        // ---------- BOTÃO CADASTRAR (200ms, 0.9) ----------
        btnCadastrar.setOnClickListener {
            val scaleX = ObjectAnimator.ofFloat(btnCadastrar, "scaleX", 1f, 0.9f, 1f)
            val scaleY = ObjectAnimator.ofFloat(btnCadastrar, "scaleY", 1f, 0.9f, 1f)
            AnimatorSet().apply {
                playTogether(scaleX, scaleY)
                duration = 200
                start()
            }

            val email = etEmail.text.toString().trim()
            val usuario = etUsuario.text.toString().trim()
            val senha = etSenha.text.toString()
            val confirmaSenha = etConfirmaSenha.text.toString()

            if (email.isEmpty() || usuario.isEmpty() || senha.isEmpty() || confirmaSenha.isEmpty()) {
                Toast.makeText(this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (senha != confirmaSenha) {
                Toast.makeText(this, "As senhas não conferem!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            cadastrarUsuarioFirebase(email, usuario, senha)
        }
    }

    private fun cadastrarUsuarioFirebase(email: String, usuario: String, senha: String) {
        auth.createUserWithEmailAndPassword(email, senha)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    val uid = firebaseUser?.uid

                    if (uid != null) {
                        salvarDadosAdicionaisUsuario(uid, usuario, email)
                    }
                } else {
                    val exception = task.exception
                    Toast.makeText(baseContext, "Erro: ${exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun salvarDadosAdicionaisUsuario(uid: String, username: String, email: String) {
        val db = FirebaseFirestore.getInstance()
        val dadosUsuario = hashMapOf(
            "username" to username,
            "email" to email
        )

        db.collection("users").document(uid)
            .set(dadosUsuario)
            .addOnSuccessListener {
                Toast.makeText(this, "Usuário cadastrado com sucesso!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao salvar informações adicionais: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
