package com.example.anotacao.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.anotacao.Email_verificacao
import com.example.anotacao.MainActivity
import com.example.anotacao.R

class Pag2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_cadastro)

        val txtVoltar = findViewById<TextView>(R.id.txtVoltar)
        txtVoltar.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }


        val imgVoltar = findViewById<ImageView>(R.id.imgVoltar)
        imgVoltar.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        val btnCadastrar = findViewById<Button>(R.id.btnCadastrar)
        btnCadastrar.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }





    }
}