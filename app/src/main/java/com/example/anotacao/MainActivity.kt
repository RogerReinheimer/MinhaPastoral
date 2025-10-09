package com.example.anotacao

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.anotacao.ui.login.Pag_Cadastro

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pag_entrar)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnEntrar = findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.btnEntrar)

        btnEntrar.setOnClickListener {
            val intent = Intent(this, Pag_home::class.java)
            startActivity(intent)
        }

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
    }

}



