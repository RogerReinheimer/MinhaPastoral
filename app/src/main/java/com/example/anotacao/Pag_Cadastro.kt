package com.example.anotacao.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText // Importação necessária
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast // Importação necessária
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope // Importação necessária
import com.example.anotacao.MainActivity
import com.example.anotacao.R
import com.example.anotacao.data.AppDatabase // Importação do seu Banco
import com.example.anotacao.data.User // Importação da sua Entidade
import kotlinx.coroutines.launch // Importação necessária

class Pag_Cadastro : AppCompatActivity() {

    // 1. ACESSO AO BANCO DE DADOS
    // Criamos uma referência para o nosso DAO.
    // O 'by lazy' é uma forma eficiente de garantir que o banco só será
    // inicializado quando formos usá-lo pela primeira vez.
    private val userDao by lazy { AppDatabase.getDatabase(this).userDao() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_cadastro)

        // --- Lógica para Voltar (Melhorada) ---
        val txtVoltar = findViewById<TextView>(R.id.txtVoltar)
        txtVoltar.setOnClickListener {
            // finish() é melhor para "voltar", pois apenas fecha a tela atual.
            finish()
        }
        val imgVoltar = findViewById<ImageView>(R.id.imgVoltar)
        imgVoltar.setOnClickListener {
            finish()
        }

        // 2. PEGANDO AS REFERÊNCIAS DOS CAMPOS DE TEXTO
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etUsuario = findViewById<EditText>(R.id.etUsuario)
        val etSenha = findViewById<EditText>(R.id.etSenha)
        val etConfirmaSenha = findViewById<EditText>(R.id.etConfirmaSenha)
        val btnCadastrar = findViewById<Button>(R.id.btnCadastrar)

        // 3. ADICIONANDO A LÓGICA AO BOTÃO DE CADASTRO
        btnCadastrar.setOnClickListener {
            val email = etEmail.text.toString()
            val usuario = etUsuario.text.toString()
            val senha = etSenha.text.toString()
            val confirmaSenha = etConfirmaSenha.text.toString()

            // 4. VALIDAÇÃO DOS DADOS
            if (email.isNotEmpty() && usuario.isNotEmpty() && senha.isNotEmpty()) {
                if (senha == confirmaSenha) {
                    // Se a validação passar, chamamos a função para salvar no banco.
                    cadastrarNovoUsuario(email, usuario, senha)
                } else {
                    Toast.makeText(this, "As senhas não conferem!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    // 5. FUNÇÃO QUE SALVA O USUÁRIO NO BANCO DE DADOS
    private fun cadastrarNovoUsuario(email: String, usuario: String, senha: String) {
        // Operações de banco de dados PRECISAM ser feitas fora da thread principal
        // para não travar o app. Usamos Coroutines (lifecycleScope.launch) para isso.
        lifecycleScope.launch {
            // Primeiro, verificamos se o nome de usuário ou e-mail já existem
            val usuarioExistente = userDao.findByUsername(usuario)
            val emailExistente = userDao.findByEmail(email)

            if (usuarioExistente != null) {
                Toast.makeText(
                    this@Pag_Cadastro,
                    "Este nome de usuário já está em uso.",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (emailExistente != null) {
                Toast.makeText(
                    this@Pag_Cadastro,
                    "Este e-mail já foi cadastrado.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                // Se tudo estiver livre, criamos o objeto User
                val novoUsuario = User(
                    email = email,
                    username = usuario,
                    // IMPORTANTE: Em um app real, a senha deve ser criptografada antes de salvar!
                    // Por agora, vamos salvá-la como texto puro para fins de aprendizado.
                    passwordHash = senha
                )
                // Inserimos o usuário no banco de dados
                userDao.insert(novoUsuario)

                // Damos um feedback de sucesso e fechamos a tela (AGORA CORRIGIDO)
                Toast.makeText(
                    this@Pag_Cadastro,
                    "Usuário cadastrado com sucesso!",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }
}