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
import androidx.lifecycle.lifecycleScope
import com.example.anotacao.data.AppDatabase
import com.example.anotacao.ui.login.Pag_Cadastro
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    // 1. ACESSO AO BANCO DE DADOS (igual fizemos na tela de cadastro)
    private val userDao by lazy { AppDatabase.getDatabase(this).userDao() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pag_entrar)

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

        // 2. PEGANDO AS REFERÊNCIAS DOS CAMPOS DE LOGIN
        val etUsuario = findViewById<EditText>(R.id.etEmailUsuario)
        val etSenha = findViewById<EditText>(R.id.etSenha)
        val btnEntrar = findViewById<Button>(R.id.btnEntrar)

        // 3. LÓGICA DO BOTÃO ENTRAR (MODIFICADA)
        btnEntrar.setOnClickListener {
            val usuarioDigitado = etUsuario.text.toString()
            val senhaDigitada = etSenha.text.toString()

            // Validação para garantir que os campos não estão vazios
            if (usuarioDigitado.isNotEmpty() && senhaDigitada.isNotEmpty()) {
                realizarLogin(usuarioDigitado, senhaDigitada)
            } else {
                Toast.makeText(this, "Por favor, preencha o usuário e a senha.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 4. NOVA FUNÇÃO PARA VERIFICAR O LOGIN
    private fun realizarLogin(usuarioDigitado: String, senhaDigitada: String) {
        // Usamos coroutines (lifecycleScope) para acessar o banco fora da thread principal
        lifecycleScope.launch {
            // Usamos a função do DAO para buscar um usuário pelo nome.
            // Por enquanto, a busca será feita apenas pelo NOME DE USUÁRIO.
            val usuarioEncontrado = userDao.findByUsername(usuarioDigitado)

            // Verificamos duas coisas:
            // 1. Se o usuário foi encontrado (não é nulo)
            // 2. Se a senha do usuário encontrado é igual à senha digitada
            if (usuarioEncontrado != null && usuarioEncontrado.passwordHash == senhaDigitada) {
                // SUCESSO!
                Toast.makeText(this@MainActivity, "Login bem-sucedido!", Toast.LENGTH_SHORT).show()

                // Navega para a tela principal do app
                val intent = Intent(this@MainActivity, Pag_home::class.java)
                startActivity(intent)

                // Fecha a tela de login para que o usuário não possa voltar para ela
                finish()
            } else {
                // FALHA!
                Toast.makeText(this@MainActivity, "Usuário ou senha inválidos.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}