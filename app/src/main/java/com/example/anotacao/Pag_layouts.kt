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
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

                        val btnExcluir = item.findViewById<Button>(R.id.btn_excluir)
                        btnExcluir.setOnClickListener {
                            db.collection("mensagens").document(doc.id)
                                .delete()
                                .addOnSuccessListener {
                                    containerMensagensDia.removeView(item)
                                    Toast.makeText(this, "Mensagem excluída com sucesso!", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, "Erro ao excluir mensagem.", Toast.LENGTH_SHORT).show()
                                }
                        }

                        val btnPostar = item.findViewById<Button>(R.id.btn_postar)
                        btnPostar.setOnClickListener {
                            val tituloMensagem = doc.getString("titulo") ?: "(sem título)"
                            val textoMensagem = doc.getString("texto") ?: ""
                            val dataAtual = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

                            val mensagemMap = hashMapOf(
                                "titulo" to tituloMensagem,
                                "data" to dataAtual,
                                "texto" to textoMensagem,
                                "timestamp" to FieldValue.serverTimestamp()
                            )

                            db.collection("mensagemDoDia").document("atual")
                                .set(mensagemMap)
                                .addOnSuccessListener {
                                    db.collection("mensagensPostadas")
                                        .add(mensagemMap)
                                        .addOnSuccessListener {
                                            Toast.makeText(this, "Mensagem postada com sucesso!", Toast.LENGTH_SHORT).show()
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(this, "Erro ao salvar histórico.", Toast.LENGTH_SHORT).show()
                                        }
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, "Erro ao postar mensagem do dia.", Toast.LENGTH_SHORT).show()
                                }
                        }

                        containerMensagensDia.addView(item)
                    }
                }

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

                        val btnExcluir = item.findViewById<Button>(R.id.btn_excluir)
                        btnExcluir.setOnClickListener {
                            db.collection("lemaDoAno").document(doc.id)
                                .delete()
                                .addOnSuccessListener {
                                    containerLemaAno.removeView(item)
                                    Toast.makeText(this, "Lema excluído!", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, "Erro ao excluir lema.", Toast.LENGTH_SHORT).show()
                                }
                        }

                        val btnPostar = item.findViewById<Button>(R.id.btn_postar)
                        btnPostar.setOnClickListener {
                            val dataAtual = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

                            val lemaMap = hashMapOf(
                                "titulo" to titulo,
                                "data" to dataAtual,
                                "lema" to lema,
                                "timestamp" to FieldValue.serverTimestamp()
                            )

                            // Atualiza o lema atual (mostrado na tela de Home)
                            db.collection("lemaAtual").document("atual")
                                .set(lemaMap)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Lema do ano atualizado!", Toast.LENGTH_SHORT).show()

                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, "Erro ao postar lema do ano.", Toast.LENGTH_SHORT).show()
                                }
                        }

                        containerLemaAno.addView(item)
                    }
                }


            listenerAnotacao = db.collection("anotacoes")
                .whereEqualTo("uid", uid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    if (snapshot == null) return@addSnapshotListener

                    containerAnotacao.removeAllViews()
                    val caixaFavoritos = findViewById<LinearLayout>(R.id.layout_caixa1)

                    val inflater = LayoutInflater.from(this)

                    for (doc in snapshot.documents) {
                        val titulo = doc.getString("titulo") ?: "(sem título)"
                        val texto = doc.getString("texto") ?: ""
                        val favoritoFirestore = doc.getBoolean("favorito") ?: false

                        val item = inflater.inflate(
                            R.layout.card_anotacao_salva,
                            containerAnotacao,
                            false
                        )

                        val tvTitulo = item.findViewById<TextView>(R.id.tv_titulo_anotacao_salva)
                        val tvConteudo = item.findViewById<TextView>(R.id.tv_conteudo_anotacao_salvo)
                        val estrela = item.findViewById<ImageView>(R.id.iv_estrela)
                        val btnExcluir = item.findViewById<Button>(R.id.btn_excluir) // botão de excluir

                        tvTitulo.text = titulo
                        tvConteudo.text = texto
                        tvConteudo.visibility = View.GONE

                        var favorito = favoritoFirestore

                        // Define aparência inicial da estrela e posição
                        if (favorito) {
                            estrela.setImageResource(android.R.drawable.btn_star_big_on)
                            estrela.setColorFilter(android.graphics.Color.parseColor("#FFD700"))
                            caixaFavoritos.addView(item)
                        } else {
                            estrela.setImageResource(android.R.drawable.btn_star_big_off)
                            estrela.setColorFilter(android.graphics.Color.parseColor("#B0B0B0"))
                            containerAnotacao.addView(item)
                        }

                        item.setOnClickListener {
                            tvConteudo.visibility =
                                if (tvConteudo.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                        }

                        estrela.setOnClickListener {
                            favorito = !favorito

                            if (favorito) {
                                estrela.setImageResource(android.R.drawable.btn_star_big_on)
                                estrela.setColorFilter(android.graphics.Color.parseColor("#FFD700"))
                            } else {
                                estrela.setImageResource(android.R.drawable.btn_star_big_off)
                                estrela.setColorFilter(android.graphics.Color.parseColor("#B0B0B0"))
                            }

                            db.collection("anotacoes").document(doc.id)
                                .update("favorito", favorito)
                                .addOnFailureListener {
                                    Toast.makeText(
                                        this,
                                        "Erro ao atualizar favorito",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                            val parent = item.parent as? LinearLayout
                            parent?.removeView(item)

                            if (favorito) {
                                caixaFavoritos.addView(item)
                            } else {
                                containerAnotacao.addView(item, 0)
                            }

                            caixaFavoritos.visibility =
                                if (caixaFavoritos.childCount > 1) View.VISIBLE else View.GONE
                        }

                        btnExcluir.setOnClickListener {
                            db.collection("anotacoes").document(doc.id)
                                .delete()
                                .addOnSuccessListener {
                                    val parent = item.parent as? LinearLayout
                                    parent?.removeView(item)
                                    Toast.makeText(this, "Anotação excluída com sucesso!", Toast.LENGTH_SHORT)
                                        .show()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, "Erro ao excluir anotação.", Toast.LENGTH_SHORT)
                                        .show()
                                }
                        }
                    }

                    caixaFavoritos.visibility =
                        if (caixaFavoritos.childCount > 1) View.VISIBLE else View.GONE
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

        // Botão "Configurações"
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
