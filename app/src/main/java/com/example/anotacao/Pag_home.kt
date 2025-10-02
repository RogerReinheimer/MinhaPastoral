package com.example.anotacao

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialog


class Pag_home : AppCompatActivity() {

    private var isExpanded1 = false
    private var isExpanded2 = false
    private var isExpanded3 = false
    private var isExpanded4 = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pag_home)

        val Cabecario_LA = findViewById<View>(R.id.Cabecario_LA)
        val Conteudo_LA = findViewById<View>(R.id.Conteudo_LA)

        val Cabecario_MD = findViewById<View>(R.id.Cabecario_MD)
        val Conteudo_MD = findViewById<View>(R.id.Conteudo_MD)

        val Cabecario_AT = findViewById<View>(R.id.Cabecario_AT)
        val Conteudo_AT = findViewById<View>(R.id.Conteudo_AT)

        val Cabecario_NT = findViewById<View>(R.id.Cabecario_NT)
        val Conteudo_NT = findViewById<View>(R.id.Conteudo_NT)


        Cabecario_LA.setOnClickListener {
            if (isExpanded1) {
                Conteudo_LA.visibility = View.GONE
            } else {
                Conteudo_LA.visibility = View.VISIBLE
            }
            isExpanded1 = !isExpanded1
        }

        Cabecario_MD.setOnClickListener {
            if (isExpanded2) {
                Conteudo_MD.visibility = View.GONE
            } else {
                Conteudo_MD.visibility = View.VISIBLE
            }
            isExpanded2 = !isExpanded2
        }

        Cabecario_AT.setOnClickListener {
            if (isExpanded3) {
                Conteudo_AT.visibility = View.GONE
            } else {
                Conteudo_AT.visibility = View.VISIBLE
            }
            isExpanded3 = !isExpanded3
        }

        Cabecario_NT.setOnClickListener {
            if (isExpanded4) {
                Conteudo_NT.visibility = View.GONE
            } else {
                Conteudo_NT.visibility = View.VISIBLE
            }
            isExpanded4 = !isExpanded4
        }

        val btnBiblia = findViewById<ImageView>(R.id.btnMensagens)
        val btnCruz = findViewById<ImageView>(R.id.btnMensagensSemana)

        btnBiblia.setOnClickListener {
            val intent = Intent(this, Mensagens::class.java)
            startActivity(intent)
        }

        btnCruz.setOnClickListener {
            val intent = Intent(this, Mensagens_semana::class.java)
            startActivity(intent)
        }

        val btnMenu = findViewById<ImageView>(R.id.btnMenu)
        btnMenu.setOnClickListener {
            mostrarSheetLateral()
        }

        //botao mais
        val btnMais = findViewById<ImageView>(R.id.btnFlutuante)
        btnMais.setOnClickListener {
            mostrarSheetOpcoes()
        }

    }//oncreate

    private fun mostrarSheetLateral() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.bottom_sheet_layout)

        dialog.window?.apply {
            setLayout(
                (resources.displayMetrics.widthPixels * 0.7).toInt(), // 70% da largura da tela
                WindowManager.LayoutParams.MATCH_PARENT
            )
            setGravity(Gravity.END) // abre na lateral direita
            attributes.windowAnimations = R.style.DialogAnimationDireita
        }

        dialog.show()
    }

    //função opções
    private fun mostrarSheetOpcoes() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_opcoes, null)

        val opcaoAnotacao = view.findViewById<TextView>(R.id.opcaoAnotacao)
        val opcaoLema = view.findViewById<TextView>(R.id.opcaoLema)
        val opcaoMensagem = view.findViewById<TextView>(R.id.opcaoMensagem)

        opcaoAnotacao.setOnClickListener {
            //redirecionar
            val intent = Intent(this, MainAnotacao::class.java)
            startActivity(intent)
            // ação para Adicionar Anotação
            bottomSheetDialog.dismiss()
        }

        opcaoLema.setOnClickListener {
            //redirecionar
            val intent = Intent(this, PredefinicaoLema::class.java)
            startActivity(intent)
            // ação para Adicionar Lema
            bottomSheetDialog.dismiss()
        }

        opcaoMensagem.setOnClickListener {
            //redirecionar
            val intent = Intent(this, PredefinicaoMsg::class.java)
            startActivity(intent)
            // ação para Adicionar Mensagem
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.show()
    }

}//fim class