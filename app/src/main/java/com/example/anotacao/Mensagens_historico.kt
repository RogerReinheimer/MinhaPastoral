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
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.widget.addTextChangedListener
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.io.File

class Mensagens_historico : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()

    // Variáveis para paginação
    private var lastVisible: DocumentSnapshot? = null
    private val ITEMS_PER_PAGE = 7
    private var isLoading = false
    private var hasMoreData = true
    private val listaMensagensCarregadas = mutableListOf<Map<String, Any>>()
    private var estaPesquisando = false

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mensagens_historico)

        val btnHome = findViewById<ImageView>(R.id.btnPagHome3)
        val btnBiblia = findViewById<ImageView>(R.id.btnMensagens3)
        val btnMais = findViewById<ImageView>(R.id.btnFlutuante)
        val btnMenu = findViewById<ImageView>(R.id.btnMenu)

        btnHome.setOnClickListener {
            startActivity(Intent(this, Pag_home::class.java))
        }

        btnBiblia.setOnClickListener {
            startActivity(Intent(this, Mensagens::class.java))
        }

        btnMais.setOnClickListener {
            mostrarSheetOpcoes()
        }

        btnMenu.setOnClickListener {
            mostrarSheetLateral()
        }

        carregarMensagemDoDia()
        configurarPesquisa()
        carregarMensagens(false)
    }

    private fun carregarMensagens(isLoadMore: Boolean) {
        if (isLoading) return

        if (!isLoadMore) {
            listaMensagensCarregadas.clear()
            lastVisible = null
            hasMoreData = true
        }

        isLoading = true

        var query = db.collection("mensagensPostadas")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(ITEMS_PER_PAGE.toLong())

        if (isLoadMore && lastVisible != null) {
            query = query.startAfter(lastVisible!!)
        }

        query.get().addOnSuccessListener { snapshot ->
            if (snapshot.isEmpty) {
                hasMoreData = false
                if (isLoadMore) {
                    Toast.makeText(this, "Não há mais mensagens", Toast.LENGTH_SHORT).show()
                }
                isLoading = false
                atualizarListaMensagens()
                return@addOnSuccessListener
            }

            lastVisible = snapshot.documents[snapshot.size() - 1]

            for (doc in snapshot.documents) {
                val titulo = doc.getString("titulo") ?: "(sem título)"
                val data = doc.getString("data") ?: ""
                val texto = doc.getString("texto") ?: ""

                listaMensagensCarregadas.add(
                    mapOf(
                        "id" to doc.id,
                        "titulo" to titulo,
                        "data" to data,
                        "texto" to texto
                    )
                )
            }

            if (snapshot.size() < ITEMS_PER_PAGE) {
                hasMoreData = false
            }

            isLoading = false
            atualizarListaMensagens()

        }.addOnFailureListener {
            Toast.makeText(this, "Erro ao carregar mensagens", Toast.LENGTH_SHORT).show()
            isLoading = false
        }
    }

    private fun atualizarListaMensagens() {
        val containerHistorico = findViewById<LinearLayout>(R.id.container_historico)
        val inflater = layoutInflater

        containerHistorico.removeAllViews()

        if (listaMensagensCarregadas.isEmpty()) {
            val vazio = TextView(this)
            vazio.text = "Nenhuma mensagem postada ainda."
            vazio.textAlignment = View.TEXT_ALIGNMENT_CENTER
            vazio.setTextColor(android.graphics.Color.WHITE)
            vazio.setPadding(0, 24, 0, 24)
            containerHistorico.addView(vazio)
            return
        }

        for ((index, msg) in listaMensagensCarregadas.withIndex()) {
            adicionarCardMensagem(msg, index, containerHistorico, inflater)
        }

        if (!estaPesquisando) {
            adicionarBotoesNavegacao(containerHistorico)
        }
    }

    private fun adicionarCardMensagem(
        msg: Map<String, Any>,
        index: Int,
        container: LinearLayout,
        inflater: LayoutInflater
    ) {
        val item = inflater.inflate(R.layout.card_mensagem_interno, container, false)

        val tvTitulo = item.findViewById<TextView>(R.id.tv_titulo1)
        val tvData = item.findViewById<TextView>(R.id.tv_info1)
        val tvConteudo = item.findViewById<TextView>(R.id.Conteudo_MDI)
        val cabecario = item.findViewById<LinearLayout>(R.id.Cabecario_MDI)

        tvTitulo.text = msg["titulo"] as String
        tvData.text = msg["data"] as String
        tvConteudo.text = msg["texto"] as String

        cabecario.setOnClickListener {
            tvConteudo.visibility =
                if (tvConteudo.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }

        cabecario.setOnLongClickListener {
            val docId = msg["id"] as String
            db.collection("mensagensPostadas").document(docId)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(this, "Mensagem excluída!", Toast.LENGTH_SHORT).show()
                    if (estaPesquisando) {
                        val etPesquisa = findViewById<EditText>(R.id.et_caixa_pesquisa)
                        pesquisarTodasMensagens(etPesquisa.text.toString())
                    } else {
                        carregarMensagens(false)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Erro ao excluir.", Toast.LENGTH_SHORT).show()
                }
            true
        }

        container.addView(item)
    }

    private fun adicionarBotoesNavegacao(container: LinearLayout) {
        // Botão "Mostrar Mais"
        if (hasMoreData && !isLoading) {
            val btnMostrarMais = TextView(this)
            btnMostrarMais.text = "▼ Mostrar mais mensagens"
            btnMostrarMais.textAlignment = View.TEXT_ALIGNMENT_CENTER
            btnMostrarMais.setTextColor(android.graphics.Color.WHITE)
            btnMostrarMais.textSize = 15f
            btnMostrarMais.setTypeface(null, android.graphics.Typeface.BOLD)
            btnMostrarMais.setPadding(16, 32, 16, 16)

            btnMostrarMais.setOnClickListener {
                carregarMensagens(true)
            }

            container.addView(btnMostrarMais)
        }

        // Botão "Esconder"
        if (listaMensagensCarregadas.size > ITEMS_PER_PAGE) {
            val btnEsconder = TextView(this)
            btnEsconder.text = "▲ Esconder mensagens extras"
            btnEsconder.textAlignment = View.TEXT_ALIGNMENT_CENTER
            btnEsconder.setTextColor(android.graphics.Color.parseColor("#90CAF9"))
            btnEsconder.textSize = 15f
            btnEsconder.setTypeface(null, android.graphics.Typeface.BOLD)
            btnEsconder.setPadding(16, 16, 16, 32)

            btnEsconder.setOnClickListener {
                carregarMensagens(false)
            }

            container.addView(btnEsconder)
        }
    }

    private fun configurarPesquisa() {
        val etPesquisa = findViewById<EditText>(R.id.et_caixa_pesquisa)

        etPesquisa.addTextChangedListener { texto ->
            val filtro = texto.toString().trim()

            // Se o campo está vazio OU tem apenas espaços, volta para modo normal
            if (filtro.isEmpty() || filtro.isBlank()) {
                if (estaPesquisando) {  // Só recarrega se estava pesquisando
                    estaPesquisando = false
                    carregarMensagens(false)
                }
                return@addTextChangedListener
            }

            // Ativa modo pesquisa
            estaPesquisando = true
            pesquisarTodasMensagens(filtro)
        }
    }

    private fun pesquisarTodasMensagens(termoPesquisa: String) {
        val containerHistorico = findViewById<LinearLayout>(R.id.container_historico)
        val inflater = layoutInflater

        db.collection("mensagensPostadas")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                containerHistorico.removeAllViews()

                val resultadosFiltrados = mutableListOf<Map<String, Any>>()
                val filtroLower = termoPesquisa.lowercase()

                for (doc in snapshot.documents) {
                    val titulo = doc.getString("titulo") ?: ""
                    val data = doc.getString("data") ?: ""
                    val texto = doc.getString("texto") ?: ""

                    if (titulo.lowercase().contains(filtroLower) ||
                        data.lowercase().contains(filtroLower) ||
                        texto.lowercase().contains(filtroLower)) {

                        resultadosFiltrados.add(
                            mapOf(
                                "id" to doc.id,
                                "titulo" to titulo,
                                "data" to data,
                                "texto" to texto
                            )
                        )
                    }
                }

                if (resultadosFiltrados.isEmpty()) {
                    val vazio = TextView(this)
                    vazio.text = "Nenhuma mensagem encontrada"
                    vazio.textAlignment = View.TEXT_ALIGNMENT_CENTER
                    vazio.setTextColor(android.graphics.Color.WHITE)
                    vazio.setPadding(16, 24, 16, 24)
                    containerHistorico.addView(vazio)
                } else {
                    val tvResultados = TextView(this)
                    tvResultados.text = "${resultadosFiltrados.size} mensagem(ns) encontrada(s)"
                    tvResultados.textAlignment = View.TEXT_ALIGNMENT_CENTER
                    tvResultados.setTextColor(android.graphics.Color.parseColor("#90CAF9"))
                    tvResultados.textSize = 13f
                    tvResultados.setTypeface(null, android.graphics.Typeface.ITALIC)
                    tvResultados.setPadding(0, 16, 0, 16)
                    containerHistorico.addView(tvResultados)

                    for ((index, msg) in resultadosFiltrados.withIndex()) {
                        adicionarCardMensagem(msg, index, containerHistorico, inflater)
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao pesquisar mensagens", Toast.LENGTH_SHORT).show()
            }
    }

    private fun carregarMensagemDoDia() {
        val tvTitulo = findViewById<TextView>(R.id.tvSubtitulo)
        val tvData = findViewById<TextView>(R.id.tvData)
        val tvTexto = findViewById<TextView>(R.id.Conteudo_MD)
        val cabecario = findViewById<LinearLayout>(R.id.Cabecario_MD)

        db.collection("mensagemDoDia")
            .document("atual")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    tvTitulo.text = snapshot.getString("titulo") ?: "Sem título"
                    tvData.text = snapshot.getString("data") ?: "--/--/----"
                    tvTexto.text = snapshot.getString("texto") ?: "Nenhum texto disponível."
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

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    txtPerfilNome.text = "Olá, ${document.getString("username") ?: "usuário"}!"
                }
        }

        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val caminhoFoto = prefs.getString("foto_local", null)

        if (caminhoFoto != null) {
            val file = File(caminhoFoto)
            if (file.exists() && file.length() > 0) {
                Glide.with(this)
                    .load(file)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
                    .circleCrop()
                    .placeholder(R.drawable.img_3)
                    .error(R.drawable.img_3)
                    .into(imgPerfil)
            } else {
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

    private fun mostrarSheetOpcoes() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_opcoes, null)

        val opcaoAnotacao = view.findViewById<TextView>(R.id.opcaoAnotacao)
        val opcaoLema = view.findViewById<TextView>(R.id.opcaoLema)
        val opcaoMensagem = view.findViewById<TextView>(R.id.opcaoMensagem)

        opcaoAnotacao.setOnClickListener {
            val intent = Intent(this, MainAnotacao::class.java)
            startActivity(intent)
            bottomSheetDialog.dismiss()
        }

        opcaoLema.setOnClickListener {
            val intent = Intent(this, PredefinicaoLema::class.java)
            startActivity(intent)
            bottomSheetDialog.dismiss()
        }

        opcaoMensagem.setOnClickListener {
            val intent = Intent(this, PredefinicaoMsg::class.java)
            startActivity(intent)
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.show()
    }
}
