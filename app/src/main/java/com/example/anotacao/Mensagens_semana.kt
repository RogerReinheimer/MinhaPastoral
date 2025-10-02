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

class Mensagens_semana : AppCompatActivity() {

    private var isExpanded1 = false
    private var isExpanded2 = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mensagens_semana)

        val Cabecario_MD = findViewById<View>(R.id.Cabecario_MD)
        val Conteudo_MD = findViewById<View>(R.id.Conteudo_MD)

        val Cabecario_MDI = findViewById<View>(R.id.Cabecario_MDI)
        val Conteudo_MDI = findViewById<View>(R.id.Conteudo_MDI)

        Cabecario_MD.setOnClickListener {
            if (isExpanded1) {
                Conteudo_MD.visibility = View.GONE
            } else {
                Conteudo_MD.visibility = View.VISIBLE
            }
            isExpanded1 = !isExpanded1
        }

        Cabecario_MDI.setOnClickListener {
            if (isExpanded2) {
                Conteudo_MDI.visibility = View.GONE
            } else {
                Conteudo_MDI.visibility = View.VISIBLE
            }
            isExpanded2 = !isExpanded2
        }

        val btnHome = findViewById<ImageView>(R.id.btnPagHome3)
        val btnBiblia = findViewById<ImageView>(R.id.btnMensagens3)


        btnHome.setOnClickListener {
            val intent = Intent(this, Pag_home::class.java)
            startActivity(intent)
        }

        btnBiblia.setOnClickListener {
            val intent = Intent(this, Mensagens::class.java)
            startActivity(intent)
        }

        //botao mais

        val btnMais = findViewById<ImageView>(R.id.btnFlutuante)
        btnMais.setOnClickListener {
            mostrarSheetOpcoes()
        }

        val btnMenu = findViewById<ImageView>(R.id.btnMenu)
        btnMenu.setOnClickListener {
            mostrarSheetLateral()
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
}//fim da class