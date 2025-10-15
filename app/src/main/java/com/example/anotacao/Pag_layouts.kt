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
    private var listenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null
    private var listenerLema: com.google.firebase.firestore.ListenerRegistration? = null

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

        layoutMensagemDia.setOnClickListener {
            containerMensagensDia.visibility =
                if (containerMensagensDia.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }

        layoutLemaAno.setOnClickListener {
            containerLemaAno.visibility =
                if (containerLemaAno.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }

        val uid = auth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "Usuário não autenticado", Toast.LENGTH_SHORT).show()
        } else {
            val inflater = LayoutInflater.from(this)

            // 🔹 Listener das mensagens
            listenerRegistration = db.collection("mensagens")
                .whereEqualTo("uid", uid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Toast.makeText(this, "Erro ao carregar mensagens", Toast.LENGTH_SHORT).show()
                        return@addSnapshotListener
                    }
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

            // 🔹 Listener do lema do ano (mesma lógica das mensagens)
            listenerLema = db.collection("lemaDoAno")
                .whereEqualTo("uid", uid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Toast.makeText(this, "Erro ao carregar lema do ano", Toast.LENGTH_SHORT).show()
                        return@addSnapshotListener
                    }
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
        }

        btnMais.setOnClickListener { mostrarSheetOpcoes() }
        btnMenu.setOnClickListener { mostrarSheetLateral() }
        btnBiblia.setOnClickListener { startActivity(Intent(this, Mensagens::class.java)) }
        btnPagHome.setOnClickListener { startActivity(Intent(this, Pag_home::class.java)) }
        btnCruz.setOnClickListener { startActivity(Intent(this, Mensagens_semana::class.java)) }
    }

    override fun onDestroy() {
        super.onDestroy()
        listenerRegistration?.remove()
        listenerLema?.remove()
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

        // Botão "Layouts"
        val opcLayout = dialog.findViewById<LinearLayout>(R.id.layoutLayout)
        opcLayout.setOnClickListener {
            val intent = Intent(this, Pag_layouts::class.java)
            startActivity(intent)
            dialog.dismiss()
        }

        // Botão "Configurações"
        val opcConfig = dialog.findViewById<LinearLayout>(R.id.layoutConfig)
        opcConfig.setOnClickListener {
            val intent = Intent(this, Configuracoes::class.java)
            startActivity(intent)
            dialog.dismiss()
        }

        // Botão "Sair"
        val opcSair = dialog.findViewById<LinearLayout>(R.id.layoutSair)
        opcSair.setOnClickListener {
            FirebaseAuth.getInstance().signOut() // Desloga do Firebase

            // Vai pra tela de login e limpa o histórico
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
