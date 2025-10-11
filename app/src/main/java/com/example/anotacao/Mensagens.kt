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
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialog


class Mensagens : AppCompatActivity() {

    private var isExpanded2 = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mensagens)

        val Cabecario_MDI = findViewById<View>(R.id.Cabecario_MDI)
        val Conteudo_MDI = findViewById<View>(R.id.Conteudo_MDI)

        Cabecario_MDI.setOnClickListener {
            if (isExpanded2) {
                Conteudo_MDI.visibility = View.GONE
            } else {
                Conteudo_MDI.visibility = View.VISIBLE
            }
            isExpanded2 = !isExpanded2
        }

        val btnHome = findViewById<ImageView>(R.id.btnPagHome1)
        val btnCruz = findViewById<ImageView>(R.id.btnMensagensSemana1)

        btnHome.setOnClickListener {
            val intent = Intent(this, Pag_home::class.java)
            startActivity(intent)
        }

        btnCruz.setOnClickListener {
            val intent = Intent(this, Mensagens_semana::class.java)
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
                (resources.displayMetrics.widthPixels * 0.7).toInt(),
                WindowManager.LayoutParams.MATCH_PARENT
            )
            setGravity(Gravity.END)
            attributes.windowAnimations = R.style.DialogAnimationDireita
        }

        val opcLayout = dialog.findViewById<LinearLayout>(R.id.layoutLayout)
        opcLayout.setOnClickListener {
            val intent = Intent(this, Pag_layouts::class.java)
            startActivity(intent)
            dialog.dismiss()
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