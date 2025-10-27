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

class Mensagens_historico : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mensagens_historico)


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

        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val containerHistorico = findViewById<LinearLayout>(R.id.container_historico)
        val inflater = layoutInflater

        db.collection("mensagensPostadas")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(this, "Erro ao carregar histórico", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                containerHistorico.removeAllViews()

                if (snapshot != null && !snapshot.isEmpty) {
                    for (doc in snapshot.documents) {
                        val titulo = doc.getString("titulo") ?: "(sem título)"
                        val data = doc.getString("data") ?: ""
                        val texto = doc.getString("texto") ?: ""

                        val item = inflater.inflate(R.layout.card_mensagem_interno, containerHistorico, false)

                        val tvTitulo = item.findViewById<TextView>(R.id.tv_titulo1)
                        val tvData = item.findViewById<TextView>(R.id.tv_info1)
                        val tvConteudo = item.findViewById<TextView>(R.id.Conteudo_MDI)
                        val cabecario = item.findViewById<LinearLayout>(R.id.Cabecario_MDI)

                        tvTitulo.text = titulo
                        tvData.text = data
                        tvConteudo.text = texto

                        cabecario.setOnClickListener {
                            tvConteudo.visibility =
                                if (tvConteudo.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                        }

                        cabecario.setOnLongClickListener {
                            db.collection("mensagensPostadas").document(doc.id)
                                .delete()
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Mensagem excluída com sucesso!", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, "Erro ao excluir mensagem.", Toast.LENGTH_SHORT).show()
                                }
                            true
                        }

                        containerHistorico.addView(item)
                    }
                } else {
                    val vazio = TextView(this)
                    vazio.text = "Nenhuma mensagem postada ainda."
                    vazio.textAlignment = View.TEXT_ALIGNMENT_CENTER
                    vazio.setTextColor(android.graphics.Color.WHITE)
                    vazio.setPadding(0, 24, 0, 24)
                    containerHistorico.addView(vazio)
                }
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
            val intent = Intent(this, Pag_entrar::class.java)
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