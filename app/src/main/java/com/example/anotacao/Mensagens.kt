package com.example.anotacao

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
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
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.io.File

class Mensagens : AppCompatActivity() {

    private lateinit var btnHome: ImageView
    private lateinit var btnCruz: ImageView
    private lateinit var btnMais: ImageView
    private lateinit var btnMenu: ImageView

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mensagens)

        // ---------- FINDVIEWBYID ----------
        btnHome = findViewById(R.id.btnPagHome1)
        btnCruz = findViewById(R.id.btnMensagensSemana1)
        btnMais = findViewById(R.id.btnFlutuante)
        btnMenu = findViewById(R.id.btnMenu)

        // ---------- BOTÕES DE NAVEGAÇÃO (300ms, 0.8) ----------
        btnHome.setOnClickListener {
            val scaleX = ObjectAnimator.ofFloat(btnHome, "scaleX", 1f, 0.8f, 1f)
            val scaleY = ObjectAnimator.ofFloat(btnHome, "scaleY", 1f, 0.8f, 1f)
            AnimatorSet().apply {
                playTogether(scaleX, scaleY)
                duration = 300
                start()
            }
            startActivity(Intent(this, Pag_home::class.java))
        }

        btnCruz.setOnClickListener {
            val scaleX = ObjectAnimator.ofFloat(btnCruz, "scaleX", 1f, 0.8f, 1f)
            val scaleY = ObjectAnimator.ofFloat(btnCruz, "scaleY", 1f, 0.8f, 1f)
            AnimatorSet().apply {
                playTogether(scaleX, scaleY)
                duration = 300
                start()
            }
            startActivity(Intent(this, Mensagens_historico::class.java))
        }

        btnMais.setOnClickListener {
            val scaleX = ObjectAnimator.ofFloat(btnMais, "scaleX", 1f, 0.8f, 1f)
            val scaleY = ObjectAnimator.ofFloat(btnMais, "scaleY", 1f, 0.8f, 1f)
            AnimatorSet().apply {
                playTogether(scaleX, scaleY)
                duration = 300
                start()
            }
            mostrarSheetOpcoes()
        }

        btnMenu.setOnClickListener {
            val scaleX = ObjectAnimator.ofFloat(btnMenu, "scaleX", 1f, 0.8f, 1f)
            val scaleY = ObjectAnimator.ofFloat(btnMenu, "scaleY", 1f, 0.8f, 1f)
            AnimatorSet().apply {
                playTogether(scaleX, scaleY)
                duration = 300
                start()
            }
            mostrarSheetLateral()
        }

        // ---------- CARREGAR MENSAGENS ----------
        carregarMensagensAdmin()
    }

    private fun carregarMensagensAdmin() {
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

                    card.setOnClickListener {
                        tvConteudo.visibility =
                            if (tvConteudo.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                    }

                    // ---------- BOTÃO EXCLUIR (200ms, 0.9) ----------
                    btnExcluir.setOnClickListener {
                        val scaleX = ObjectAnimator.ofFloat(btnExcluir, "scaleX", 1f, 0.9f, 1f)
                        val scaleY = ObjectAnimator.ofFloat(btnExcluir, "scaleY", 1f, 0.9f, 1f)
                        AnimatorSet().apply {
                            playTogether(scaleX, scaleY)
                            duration = 200
                            start()
                        }
                        excluirMensagem(doc.id, titulo, data)
                    }

                    containerHistorico.addView(card)
                }
            }
    }

    private fun excluirMensagem(docId: String, titulo: String, data: String) {
        val batch = db.batch()

        val adminRef = db.collection("mensagensAdmin").document(docId)
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

        val uid = auth.currentUser?.uid
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
            auth.signOut()
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
