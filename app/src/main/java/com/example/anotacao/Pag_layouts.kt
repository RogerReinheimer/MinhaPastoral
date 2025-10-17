package com.example.anotacao

import android.app.Dialog
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class Pag_layouts : AppCompatActivity() {

    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    private var listenerMensagem: com.google.firebase.firestore.ListenerRegistration? = null
    private var listenerLema: com.google.firebase.firestore.ListenerRegistration? = null
    private var listenerAnotacao: com.google.firebase.firestore.ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pag_layouts)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnMais = findViewById<ImageView>(R.id.btnFlutuante)
        val btnMenu = findViewById<ImageView>(R.id.btnMenu)
        val btnBiblia = findViewById<ImageView>(R.id.btnMensagens2)
        val btnPagHome = findViewById<ImageView>(R.id.btnPagHome2)
        val btnCruz = findViewById<ImageView>(R.id.btnMensagensSemana2)

        val layoutMensagemDia = findViewById<LinearLayout>(R.id.layout_mensagem_dia)
        val containerMensagensDia = findViewById<LinearLayout>(R.id.container_mensagens_dia)

        val layoutLemaAno = findViewById<LinearLayout>(R.id.layout_lema_ano)
        val containerLemaAno = findViewById<LinearLayout>(R.id.container_lema_ano)

        val layoutAnotacao = findViewById<LinearLayout>(R.id.layout_conteudo_anotacao)
        val containerAnotacao = findViewById<LinearLayout>(R.id.container_anotacao)

        // expandir/recolher seções
        layoutMensagemDia.setOnClickListener {
            containerMensagensDia.visibility =
                if (containerMensagensDia.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }

        layoutLemaAno.setOnClickListener {
            containerLemaAno.visibility =
                if (containerLemaAno.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }

        layoutAnotacao.setOnClickListener {
            containerAnotacao.visibility =
                if (containerAnotacao.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }

        val uid = auth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "Usuário não autenticado", Toast.LENGTH_SHORT).show()
        } else {
            val inflater = LayoutInflater.from(this)

            // 🔹 Listener das mensagens
            listenerMensagem = db.collection("mensagens")
                .whereEqualTo("uid", uid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    if (snapshot == null) return@addSnapshotListener
                    containerMensagensDia.removeAllViews()

                    for (doc in snapshot.documents) {
                        val titulo = doc.getString("titulo") ?: "(sem título)"
                        val data = doc.getString("data") ?: "(sem data)"
                        val texto = doc.getString("texto") ?: ""

                        val item = inflater.inflate(R.layout.card_mensagem_salva, containerMensagensDia, false)
                        val tvTitulo = item.findViewById<TextView>(R.id.tv_titulo_layout_salvo)
                        val tvInfo = item.findViewById<TextView>(R.id.tv_data_layout_salvo)
                        val tvConteudo = item.findViewById<TextView>(R.id.tv_conteudo_layout_salvo)

                        tvTitulo.text = titulo
                        tvInfo.text = data
                        tvConteudo.text = texto
                        tvConteudo.visibility = View.GONE

                        item.setOnClickListener {
                            tvConteudo.visibility =
                                if (tvConteudo.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                        }
                        containerMensagensDia.addView(item)
                    }
                }

            // 🔹 Listener do lema do ano
            listenerLema = db.collection("lemaDoAno")
                .whereEqualTo("uid", uid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    if (snapshot == null) return@addSnapshotListener
                    containerLemaAno.removeAllViews()

                    for (doc in snapshot.documents) {
                        val titulo = doc.getString("titulo") ?: "(sem título)"
                        val data = doc.getString("data") ?: "(sem data)"
                        val lema = doc.getString("lema") ?: ""

                        val item = inflater.inflate(R.layout.card_mensagem_salva, containerLemaAno, false)
                        val tvTitulo = item.findViewById<TextView>(R.id.tv_titulo_layout_salvo)
                        val tvInfo = item.findViewById<TextView>(R.id.tv_data_layout_salvo)
                        val tvConteudo = item.findViewById<TextView>(R.id.tv_conteudo_layout_salvo)

                        tvTitulo.text = titulo
                        tvInfo.text = data
                        tvConteudo.text = lema
                        tvConteudo.visibility = View.GONE

                        item.setOnClickListener {
                            tvConteudo.visibility =
                                if (tvConteudo.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                        }
                        containerLemaAno.addView(item)
                    }
                }

            // 🔹 Listener das anotações (NOVO BLOCO)
            listenerAnotacao = db.collection("anotacoes")
                .whereEqualTo("uid", uid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    if (snapshot == null) return@addSnapshotListener
                    containerAnotacao.removeAllViews()

                    // pega o container específico onde tu quer que os favoritos entrem
                    val caixaFavoritos = findViewById<LinearLayout>(R.id.layout_caixa1)

                    for (doc in snapshot.documents) {
                        val titulo = doc.getString("titulo") ?: "(sem título)"
                        val texto = doc.getString("texto") ?: ""

                        val item = layoutInflater.inflate(R.layout.card_anotacao_salva, containerAnotacao, false)
                        val tvTitulo = item.findViewById<TextView>(R.id.tv_titulo_anotacao_salva)
                        val tvConteudo = item.findViewById<TextView>(R.id.tv_conteudo_anotacao_salvo)
                        val estrela = item.findViewById<ImageView>(R.id.iv_estrela)

                        tvTitulo.text = titulo
                        tvConteudo.text = texto
                        tvConteudo.visibility = View.GONE

                        // estado inicial da estrela (padrão desligada)
                        var favorito = false
                        // garante ícone e cor coerentes ao estado inicial
                        estrela.setImageResource(android.R.drawable.btn_star_big_off)
                        estrela.setColorFilter(android.graphics.Color.parseColor("#B0B0B0")) // cinza

                        // clique no item expande/recolhe conteúdo
                        item.setOnClickListener {
                            tvConteudo.visibility =
                                if (tvConteudo.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                        }

                        // clique na estrela: alterna estado e MOVE o card
                        estrela.setOnClickListener {
                            favorito = !favorito

                            // remove de qualquer parent atual antes de mover (evita crash)
                            (item.parent as? LinearLayout)?.removeView(item)

                            if (favorito) {
                                // estrela ligada + cor dourada
                                estrela.setImageResource(android.R.drawable.btn_star_big_on)
                                estrela.setColorFilter(android.graphics.Color.parseColor("#FFD700"))
                                // adiciona dentro do layout_caixa1 (substitui conteúdo anterior se quiser)
                                // aqui eu adiciono ao final do caixaFavoritos. Se tu quiser substituir o texto
                                // já existente do tv_titulo1 (ex.: mostrar apenas 1 favorito), dá pra ajustar.
                                caixaFavoritos.addView(item)
                            } else {
                                // estrela desligada + cor cinza
                                estrela.setImageResource(android.R.drawable.btn_star_big_off)
                                estrela.setColorFilter(android.graphics.Color.parseColor("#B0B0B0"))
                                // volta pro container de anotações (no topo)
                                containerAnotacao.addView(item, 0)
                            }
                        }

                        // adiciona inicialmente no container normal
                        containerAnotacao.addView(item)
                    }
                }
        }

        // botoes inferiores e sheets
        btnMais.setOnClickListener { mostrarSheetOpcoes() }
        btnMenu.setOnClickListener { mostrarSheetLateral() }
        btnBiblia.setOnClickListener { startActivity(Intent(this, Mensagens::class.java)) }
        btnPagHome.setOnClickListener { startActivity(Intent(this, Pag_home::class.java)) }
        btnCruz.setOnClickListener { startActivity(Intent(this, Mensagens_semana::class.java)) }
    }

    override fun onDestroy() {
        super.onDestroy()
        listenerMensagem?.remove()
        listenerLema?.remove()
        listenerAnotacao?.remove()
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

        val opcLayout = dialog.findViewById<LinearLayout>(R.id.layoutLayout)
        opcLayout.setOnClickListener {
            val intent = Intent(this, Pag_layouts::class.java)
            startActivity(intent)
            dialog.dismiss()
        }

        /* Botão "Configurações"
        val opcConfig = dialog.findViewById<LinearLayout>(R.id.layoutConfig)
        opcConfig.setOnClickListener {
            val intent = Intent(this, Configuracoes::class.java)
            startActivity(intent)
            dialog.dismiss()
        }*/

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

    private fun mostrarSheetOpcoes() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_opcoes, null)

        view.findViewById<TextView>(R.id.opcaoAnotacao)?.setOnClickListener {
            startActivity(Intent(this, MainAnotacao::class.java))
            bottomSheetDialog.dismiss()
        }

        view.findViewById<TextView>(R.id.opcaoLema)?.setOnClickListener {
            startActivity(Intent(this, PredefinicaoLema::class.java))
            bottomSheetDialog.dismiss()
        }

        view.findViewById<TextView>(R.id.opcaoMensagem)?.setOnClickListener {
            startActivity(Intent(this, PredefinicaoMsg::class.java))
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.show()
    }
}
