package com.example.anotacao

import android.app.Dialog
import android.content.ContentValues.TAG
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File

class Mensagens_historico : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()

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

        carregarMensagemDoDia()

        // --- PESQUISA ---
        val etPesquisa = findViewById<EditText>(R.id.et_caixa_pesquisa)
        val listaMensagens = mutableListOf<Map<String, String>>() // vai armazenar os dados originais

        db.collection("mensagensPostadas")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(this, "Erro ao carregar histórico", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                containerHistorico.removeAllViews()
                listaMensagens.clear()

                if (snapshot != null && !snapshot.isEmpty) {
                    for (doc in snapshot.documents) {
                        val titulo = doc.getString("titulo") ?: "(sem título)"
                        val data = doc.getString("data") ?: ""
                        val texto = doc.getString("texto") ?: ""

                        // guarda os dados
                        listaMensagens.add(
                            mapOf("titulo" to titulo, "data" to data, "texto" to texto)
                        )

                        // cria o card normal
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

// --- FILTRAR AO DIGITAR ---
        etPesquisa.addTextChangedListener { texto ->
            val filtro = texto.toString().trim().lowercase()

            containerHistorico.removeAllViews()

            val filtradas = if (filtro.isEmpty()) listaMensagens else
                listaMensagens.filter {
                    it["titulo"]!!.lowercase().contains(filtro) ||
                            it["data"]!!.lowercase().contains(filtro)
                }

            if (filtradas.isEmpty()) {
                val vazio = TextView(this)
                vazio.text = "Nenhuma mensagem encontrada."
                vazio.textAlignment = View.TEXT_ALIGNMENT_CENTER
                vazio.setTextColor(android.graphics.Color.WHITE)
                vazio.setPadding(0, 24, 0, 24)
                containerHistorico.addView(vazio)
            } else {
                for (msg in filtradas) {
                    val item = inflater.inflate(R.layout.card_mensagem_interno, containerHistorico, false)
                    val tvTitulo = item.findViewById<TextView>(R.id.tv_titulo1)
                    val tvData = item.findViewById<TextView>(R.id.tv_info1)
                    val tvConteudo = item.findViewById<TextView>(R.id.Conteudo_MDI)
                    val cabecario = item.findViewById<LinearLayout>(R.id.Cabecario_MDI)

                    tvTitulo.text = msg["titulo"]
                    tvData.text = msg["data"]
                    tvConteudo.text = msg["texto"]

                    cabecario.setOnClickListener {
                        tvConteudo.visibility =
                            if (tvConteudo.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                    }

                    containerHistorico.addView(item)
                }
            }
        }

    }//oncreate

    private fun carregarMensagemDoDia() {
        val tvTitulo = findViewById<TextView>(R.id.tvSubtitulo)
        val tvData = findViewById<TextView>(R.id.tvData)
        val tvTexto = findViewById<TextView>(R.id.Conteudo_MD)
        val cabecario = findViewById<LinearLayout>(R.id.Cabecario_MD)

        db.collection("mensagemDoDia")
            .document("atual")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("Pag_home", "Erro ao buscar mensagem do dia: ${e.message}")
                    Toast.makeText(this, "Falha ao carregar mensagem do dia.", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val titulo = snapshot.getString("titulo") ?: "Sem título"
                    val data = snapshot.getString("data") ?: "--/--/----"
                    val texto = snapshot.getString("texto") ?: "Nenhum texto disponível."

                    tvTitulo.text = titulo
                    tvData.text = data
                    tvTexto.text = texto
                    tvTexto.visibility = View.VISIBLE

                    cabecario.setOnClickListener {
                        tvTexto.visibility =
                            if (tvTexto.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                    }
                } else {
                    tvTitulo.text = "Sem mensagem do dia"
                    tvData.text = ""
                    tvTexto.text = ""
                    tvTexto.visibility = View.GONE
                }
            }
    }

    // ----------- MENU LATERAL -------------
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
                    .skipMemoryCache(true) // Desabilita cache de memória
                    .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE) // Desabilita cache de disco
                    .circleCrop()
                    .placeholder(R.drawable.img_3) // Imagem temporária enquanto carrega
                    .error(R.drawable.img_3)       // Imagem caso falhe
                    .into(imgPerfil)
                Log.d(TAG, "Foto carregada no menu: $caminhoFoto")
            } else {
                // Arquivo não existe ou está vazio
                Log.w(TAG, "Foto não encontrada no menu, usando padrão")
                imgPerfil.setImageResource(R.drawable.img_3)
            }
        } else {
            // Sem foto salva, usar padrão
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