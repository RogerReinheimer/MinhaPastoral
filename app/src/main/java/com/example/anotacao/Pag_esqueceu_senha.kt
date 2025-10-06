package com.example.anotacao

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class Pag_esqueceu_senha : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pag_esqueceu_senha)

        val txtVoltarParaLogin = findViewById<TextView>(R.id.txtVoltarParaLogin)
        txtVoltarParaLogin.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        val imgVoltarLogin = findViewById<ImageView>(R.id.imgVoltarLogin)
        imgVoltarLogin.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        val btnEnviar = findViewById<Button>(R.id.btnEnviar)
        btnEnviar.setOnClickListener {
            val intent = Intent(this, Email_verificacao::class.java)
            startActivity(intent)
        }


    }
}
