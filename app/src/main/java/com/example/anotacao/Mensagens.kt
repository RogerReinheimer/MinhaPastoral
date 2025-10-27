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
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


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

        val db = FirebaseFirestore.getInstance()

        // ======== ATUALIZA FOTO DE PERFIL E NOME ========
        val imgPerfil = dialog.findViewById<ImageView>(R.id.imgPerfil)
        val txtPerfilNome = dialog.findViewById<TextView>(R.id.txtPerfilNome)

        // Carrega nome do Firestore
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val nome = document.getString("username")
                        txtPerfilNome.text = "Olá, ${nome ?: "usuário"}!"
                    }
                }
        }

        // Carrega imagem local salva
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val caminhoFoto = prefs.getString("foto_local", null)
        if (caminhoFoto != null) {
            val file = java.io.File(caminhoFoto)
            if (file.exists()) {
                Glide.with(this)
                    .load(android.net.Uri.fromFile(file))
                    .circleCrop()
                    .into(imgPerfil)
            } else {
                imgPerfil.setImageResource(R.drawable.img_3) // imagem padrão
            }
        } else {
            imgPerfil.setImageResource(R.drawable.img_3)
        }

        // ======== AÇÕES DOS BOTÕES ========
        val opcLayout = dialog.findViewById<LinearLayout>(R.id.layoutLayout)
        opcLayout.setOnClickListener {
            val intent = Intent(this, Pag_layouts::class.java)
            startActivity(intent)
            dialog.dismiss()
        }

        val opcConfig = dialog.findViewById<LinearLayout>(R.id.layoutConfig)
        opcConfig.setOnClickListener {
            val intent = Intent(this, Configuracoes::class.java)
            startActivity(intent)
            dialog.dismiss()
        }

        val opcSair = dialog.findViewById<LinearLayout>(R.id.layoutSair)
        opcSair.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            dialog.dismiss()
            Toast.makeText(this, "Você saiu da conta.", Toast.LENGTH_SHORT).show()
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