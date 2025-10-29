package com.example.anotacao

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.anotacao.ui.login.Pag_Cadastro
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging

class Pag_entrar : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog
    private var senhaVisivel = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pag_entrar)

        auth = Firebase.auth
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Aguarde...")
        progressDialog.setCancelable(false)

        val usuarioAtual = auth.currentUser
        if (usuarioAtual != null) {
            startActivity(Intent(this, Pag_home::class.java))
            finish()
            return
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val txtCadastrar = findViewById<TextView>(R.id.txtCadastrar)
        val txtEsqueceuSenha = findViewById<TextView>(R.id.txtEsqueceuSenha)
        val etEmail = findViewById<EditText>(R.id.etEmailUsuario)
        val etSenha = findViewById<EditText>(R.id.etSenha)
        val btnEntrar = findViewById<Button>(R.id.btnEntrar)
        val btnMostrarSenha = findViewById<ImageView>(R.id.btnMostrarSenha)

        txtCadastrar.setOnClickListener {
            startActivity(Intent(this, Pag_Cadastro::class.java))
        }

        txtEsqueceuSenha.setOnClickListener {
            startActivity(Intent(this, Pag_esqueceu_senha::class.java))
        }

        btnEntrar.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val senha = etSenha.text.toString().trim()

            if (email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Preencha o e-mail e a senha.", Toast.LENGTH_SHORT).show()
            } else {
                btnEntrar.isEnabled = false
                progressDialog.show()
                realizarLoginFirebase(email, senha, btnEntrar)
            }
        }

        btnMostrarSenha.setOnClickListener {
            senhaVisivel = !senhaVisivel
            if (senhaVisivel) {
                etSenha.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                btnMostrarSenha.setImageResource(R.drawable.ic_visibility)
            } else {
                etSenha.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
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
                                    startActivity(Intent(this, Pag_home::class.java))
                                    finish()
                                }
                        }
                    } else {
                        startActivity(Intent(this, Pag_home::class.java))
                        finish()
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
