package com.example.anotacao

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Insira_nova_senha : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_insira_nova_senha)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val txtVoltar = findViewById<TextView>(R.id.txtVoltar)
        txtVoltar.setOnClickListener {
            val intent = Intent(this, Email_verificacao::class.java)
            startActivity(intent)
        }

        val imgVoltar = findViewById<ImageView>(R.id.imgVoltar)
        imgVoltar.setOnClickListener {
            val intent = Intent(this, Email_verificacao::class.java)
            startActivity(intent)
        }

        val btnEnviar = findViewById<Button>(R.id.btnEnviar)
        btnEnviar.setOnClickListener {
            val intent = Intent(this, Senha_alterada::class.java)
            startActivity(intent)
        }


    }
}