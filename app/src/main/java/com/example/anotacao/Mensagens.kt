package com.example.anotacao

import android.app.Dialog
import android.content.ContentValues.TAG
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.io.File


class Mensagens : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mensagens)

        val btnHome = findViewById<ImageView>(R.id.btnPagHome1)
        val btnCruz = findViewById<ImageView>(R.id.btnMensagensSemana1)

        btnHome.setOnClickListener {
            val intent = Intent(this, Pag_home::class.java)
            startActivity(intent)
        }

        btnCruz.setOnClickListener {
            val intent = Intent(this, Mensagens_historico::class.java)
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

        val db = FirebaseFirestore.getInstance()
        val containerHistorico = findViewById<LinearLayout>(R.id.container_historico)
        val inflater = LayoutInflater.from(this)

        db.collection("mensagensAdmin")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener

                containerHistorico.removeAllViews()
                for (doc in snapshot.documents) {
                    val titulo = doc.getString("titulo") ?: "(sem título)"
                    val data = doc.getString("data") ?: "(sem data)"
                    val texto = doc.getString("texto") ?: ""

                    val card = inflater.inflate(R.layout.card_mensagem_excluir, containerHistorico, false)
                    val tvTitulo = card.findViewById<TextView>(R.id.tv_titulo_layout_salvo)
                    val tvData = card.findViewById<TextView>(R.id.tv_data_layout_salvo)
                    val tvConteudo = card.findViewById<TextView>(R.id.tv_conteudo_layout_salvo)
                    val btnExcluir = card.findViewById<Button>(R.id.btn_excluir)

                    tvTitulo.text = titulo
                    tvData.text = data
                    tvConteudo.text = texto

                    // Expande/colapsa o texto
                    card.setOnClickListener {
                        tvConteudo.visibility =
                            if (tvConteudo.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                    }

                    // Excluir de todas as coleções
                    btnExcluir.setOnClickListener {
                        val batch = db.batch()

                        val adminRef = db.collection("mensagensAdmin").document(doc.id)
                        batch.delete(adminRef)

                        db.collection("mensagensPostadas")
                            .whereEqualTo("titulo", titulo)
                            .whereEqualTo("data", data)
                            .get()
                            .addOnSuccessListener { result ->
                                for (r in result) {
                                    batch.delete(r.reference)
                                }

                                db.collection("mensagemDoDia").document("atual").get()
                                    .addOnSuccessListener { atual ->
                                        val atualTitulo = atual.getString("titulo")
                                        if (atualTitulo == titulo) {
                                            batch.delete(db.collection("mensagemDoDia").document("atual"))
                                        }

                                        batch.commit().addOnSuccessListener {
                                            Toast.makeText(this, "Mensagem removida de todas as áreas.", Toast.LENGTH_SHORT).show()
                                        }.addOnFailureListener {
                                            Toast.makeText(this, "Erro ao excluir mensagem.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            }
                    }

                    containerHistorico.addView(card)
                }
            }


    }//oncreate

    // ----------- MENU LATERAL -------------
    private val db = FirebaseFirestore.getInstance() // Certifique-se que está no início da classe

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

        val imgPerfil = dialog.findViewById<ImageView>(R.id.imgPerfil)
        val txtPerfilNome = dialog.findViewById<TextView>(R.id.txtPerfilNome)

        // Carregar nome do usuário
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    txtPerfilNome.text = "Olá, ${document.getString("username") ?: "usuário"}!"
                }
        }

        // Carregar foto local (mesma lógica da tela de configurações)
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val caminhoFoto = prefs.getString("foto_local", null)

        if (caminhoFoto != null) {
            val file = File(caminhoFoto)
            if (file.exists() && file.length() > 0) {
                // Foto existe, carregar com Glide (SEM CACHE)
                Glide.with(this)
                    .load(file)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
                    .circleCrop()
                    .placeholder(R.drawable.img_3)
                    .error(R.drawable.img_3)
                    .into(imgPerfil)
                Log.d("Mensagens", "Foto carregada no menu: $caminhoFoto")
            } else {
                Log.w("Mensagens", "Foto não encontrada no menu, usando padrão")
                imgPerfil.setImageResource(R.drawable.img_3)
            }
        } else {
            imgPerfil.setImageResource(R.drawable.img_3)
        }

        dialog.findViewById<LinearLayout>(R.id.layoutLayout).setOnClickListener {
            startActivity(Intent(this, Pag_layouts::class.java))
            dialog.dismiss()
        }
        dialog.findViewById<LinearLayout>(R.id.layoutConfig).setOnClickListener {
            startActivity(Intent(this, Configuracoes::class.java))
            dialog.dismiss()
        }
        dialog.findViewById<LinearLayout>(R.id.layoutSair).setOnClickListener {
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