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
    private lateinit var btnMostrarSenha: ImageView
    private lateinit var btnMostrarConfirmaSenha: ImageView
    private lateinit var etSenha: EditText
    private lateinit var etConfirmaSenha: EditText

    private var senhaVisivel = false
    private var confirmaSenhaVisivel = false


    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_cadastro)

        auth = FirebaseAuth.getInstance()

        // ---------- FINDVIEWBYID ----------
        txtVoltar = findViewById(R.id.txtVoltar)
        imgVoltar = findViewById(R.id.imgVoltar)
        btnCadastrar = findViewById(R.id.btnCadastrar)
        btnMostrarSenha = findViewById(R.id.btnMostrarSenha)
        btnMostrarConfirmaSenha = findViewById(R.id.btnMostrarConfirmaSenha)
        etSenha = findViewById(R.id.etSenha)
        etConfirmaSenha = findViewById(R.id.etConfirmaSenha)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etUsuario = findViewById<EditText>(R.id.etUsuario)

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
                etSenha.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                btnMostrarSenha.setImageResource(R.drawable.ic_visibility)
            } else {
                etSenha.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                btnMostrarSenha.setImageResource(R.drawable.ic_visibility_off)
            }
            etSenha.setSelection(etSenha.text.length)
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
                etConfirmaSenha.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                btnMostrarConfirmaSenha.setImageResource(R.drawable.ic_visibility)
            } else {
                etConfirmaSenha.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                btnMostrarConfirmaSenha.setImageResource(R.drawable.ic_visibility_off)
            }
            etConfirmaSenha.setSelection(etConfirmaSenha.text.length)
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
