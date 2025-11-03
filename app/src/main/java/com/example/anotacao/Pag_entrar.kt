package com.example.anotacao

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.example.anotacao.core.SessionAuth
import com.example.anotacao.ui.login.Pag_Cadastro
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch

class Pag_entrar : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog
    private lateinit var txtCadastrar: TextView
    private lateinit var txtEsqueceuSenha: TextView
    private lateinit var btnEntrar: Button
    private lateinit var btnMostrarSenha: ImageView
    private lateinit var etSenha: EditText

    private var senhaVisivel = false

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pag_entrar)

        auth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Aguarde...")
        progressDialog.setCancelable(false)

        val usuarioAtual = auth.currentUser
        if (usuarioAtual != null) {
            startActivity(Intent(this, Pag_home::class.java))
            finish()
            return
        }

        // ---------- FINDVIEWBYID ----------
        txtCadastrar = findViewById(R.id.txtCadastrar)
        txtEsqueceuSenha = findViewById(R.id.txtEsqueceuSenha)
        val etEmail = findViewById<EditText>(R.id.etEmailUsuario)
        etSenha = findViewById(R.id.etSenha)
        btnEntrar = findViewById(R.id.btnEntrar)
        btnMostrarSenha = findViewById(R.id.btnMostrarSenha)

        // ---------- BOTÕES DE NAVEGAÇÃO ----------
        txtCadastrar.setOnClickListener {
            val scaleX = ObjectAnimator.ofFloat(txtCadastrar, "scaleX", 1f, 0.8f, 1f)
            val scaleY = ObjectAnimator.ofFloat(txtCadastrar, "scaleY", 1f, 0.8f, 1f)
            AnimatorSet().apply {
                playTogether(scaleX, scaleY)
                duration = 300
                start()
            }
            startActivity(Intent(this, Pag_Cadastro::class.java))
        }

        txtEsqueceuSenha.setOnClickListener {
            val scaleX = ObjectAnimator.ofFloat(txtEsqueceuSenha, "scaleX", 1f, 0.8f, 1f)
            val scaleY = ObjectAnimator.ofFloat(txtEsqueceuSenha, "scaleY", 1f, 0.8f, 1f)
            AnimatorSet().apply {
                playTogether(scaleX, scaleY)
                duration = 300
                start()
            }
            startActivity(Intent(this, Pag_esqueceu_senha::class.java))
        }

        // ---------- BOTÃO ENTRAR ----------
        btnEntrar.setOnClickListener {
            val scaleX = ObjectAnimator.ofFloat(btnEntrar, "scaleX", 1f, 0.9f, 1f)
            val scaleY = ObjectAnimator.ofFloat(btnEntrar, "scaleY", 1f, 0.9f, 1f)
            AnimatorSet().apply {
                playTogether(scaleX, scaleY)
                duration = 200
                start()
            }

            val email = etEmail.text.toString().trim()
            val senha = etSenha.text.toString().trim()

            if (email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Preencha o e-mail e a senha.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnEntrar.isEnabled = false
            progressDialog.show()
            realizarLoginFirebase(email, senha, btnEntrar)
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
                etSenha.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                btnMostrarSenha.setImageResource(R.drawable.ic_visibility)
            } else {
                etSenha.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                btnMostrarSenha.setImageResource(R.drawable.ic_visibility_off)
            }
            etSenha.setSelection(etSenha.text.length)
        }
    }

    private fun realizarLoginFirebase(email: String, senha: String, btnEntrar: Button) {
        auth.signInWithEmailAndPassword(email, senha)
            .addOnCompleteListener(this) { task ->
                progressDialog.dismiss()
                btnEntrar.isEnabled = true

                if (task.isSuccessful) {
                    Toast.makeText(this, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show()

                    val user = auth.currentUser
                    if (user != null) {
                        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                            val db = FirebaseFirestore.getInstance()
                            val dados = hashMapOf("token" to token)

                            db.collection("tokens").document(user.uid)
                                .set(dados)
                                .addOnSuccessListener {
                                    Log.d("Pag_entrar", "Token FCM salvo em 'tokens'")
                                }
                                .addOnFailureListener { e ->
                                    Log.e("Pag_entrar", "Erro ao salvar token: ${e.message}")
                                }
                                .addOnCompleteListener {
                                    // 🔒 Atualiza claims do admin antes de ir pra home
                                    lifecycleScope.launch {
                                        try { SessionAuth.refreshClaims() } catch (_: Throwable) {}
                                        startActivity(Intent(this@Pag_entrar, Pag_home::class.java))
                                        finish()
                                    }
                                }
                        }
                    } else {
                        lifecycleScope.launch {
                            try { SessionAuth.refreshClaims() } catch (_: Throwable) {}
                            startActivity(Intent(this@Pag_entrar, Pag_home::class.java))
                            finish()
                        }
                    }

                } else {
                    val erro = when (task.exception?.message) {
                        "There is no user record corresponding to this identifier. The user may have been deleted." ->
                            "Usuário não encontrado."
                        "The password is invalid or the user does not have a password." ->
                            "Senha incorreta."
                        "A network error (such as timeout, interrupted connection or unreachable host) has occurred." ->
                            "Erro de conexão. Verifique sua internet."
                        else -> "Falha no login: ${task.exception?.message}"
                    }
                    Toast.makeText(this, erro, Toast.LENGTH_LONG).show()
                }
            }
    }
}
