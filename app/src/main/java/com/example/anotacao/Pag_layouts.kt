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
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.anotacao.core.SessionAuth
import com.example.anotacao.core.AdminGate
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Pag_layouts : AppCompatActivity() {

    private lateinit var btnBiblia: ImageView
    private lateinit var btnPagHome: ImageView
    private lateinit var btnCruz: ImageView
    private lateinit var btnMais: ImageView
    private lateinit var btnMenu: ImageView
    private lateinit var containerMensagensDia: LinearLayout
    private lateinit var containerLemaAno: LinearLayout
    private lateinit var containerAnotacao: LinearLayout
    private lateinit var caixaFavoritos: LinearLayout

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var listenerMensagem: ListenerRegistration? = null
    private var listenerLema: ListenerRegistration? = null
    private var listenerAnotacao: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pag_layouts)

        // 🔒 Tela exclusiva de admin
        AdminGate.requireAdmin(this)

        // ---------- FINDVIEWBYID ----------
        btnMais = findViewById(R.id.btnFlutuante)
        btnMenu = findViewById(R.id.btnMenu)
        btnBiblia = findViewById(R.id.btnMensagens2)
        btnPagHome = findViewById(R.id.btnPagHome2)
        btnCruz = findViewById(R.id.btnMensagensSemana2)

        val layoutMensagemDia = findViewById<LinearLayout>(R.id.layout_mensagem_dia)
        containerMensagensDia = findViewById(R.id.container_mensagens_dia)

        val layoutLemaAno = findViewById<LinearLayout>(R.id.layout_lema_ano)
        containerLemaAno = findViewById(R.id.container_lema_ano)

        val layoutAnotacao = findViewById<LinearLayout>(R.id.layout_conteudo_anotacao)
        containerAnotacao = findViewById(R.id.container_anotacao)
        caixaFavoritos = findViewById(R.id.layout_caixa1)

        // ---------- CABEÇÁRIOS EXPANSÍVEIS ----------
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

        // ---------- BOTÕES DE NAVEGAÇÃO ----------
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

        // ✅ Gate no botão “+” (abre opções) — só admin
        btnMais.setOnClickListener {
            val scaleX = ObjectAnimator.ofFloat(btnMais, "scaleX", 1f, 0.8f, 1f)
            val scaleY = ObjectAnimator.ofFloat(btnMais, "scaleY", 1f, 0.8f, 1f)
            AnimatorSet().apply {
                playTogether(scaleX, scaleY)
                duration = 300
                start()
            }

            lifecycleScope.launch {
                val isAdmin = SessionAuth.isAdminFlow.value ?: false
                if (!isAdmin) {
                    Toast.makeText(
                        this@Pag_layouts,
                        "Somente administrador pode acessar estas opções.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@launch
                }
                mostrarSheetOpcoes()
            }
        }

        btnBiblia.setOnClickListener {
            val scaleX = ObjectAnimator.ofFloat(btnBiblia, "scaleX", 1f, 0.8f, 1f)
            val scaleY = ObjectAnimator.ofFloat(btnBiblia, "scaleY", 1f, 0.8f, 1f)
            AnimatorSet().apply {
                playTogether(scaleX, scaleY)
                duration = 300
                start()
            }
            startActivity(Intent(this, Mensagens::class.java))
        }

        btnPagHome.setOnClickListener {
            val scaleX = ObjectAnimator.ofFloat(btnPagHome, "scaleX", 1f, 0.8f, 1f)
            val scaleY = ObjectAnimator.ofFloat(btnPagHome, "scaleY", 1f, 0.8f, 1f)
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

        // ---------- CARREGAR DADOS ----------
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "Usuário não autenticado", Toast.LENGTH_SHORT).show()
        } else {
            carregarMensagensDia(uid)
            carregarLemaAno(uid)
            carregarAnotacoes(uid)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        listenerMensagem?.remove()
        listenerLema?.remove()
        listenerAnotacao?.remove()
    }

    private fun carregarMensagensDia(uid: String) {
        val inflater = LayoutInflater.from(this)

        listenerMensagem = db.collection("mensagens")
            .whereEqualTo("uid", uid)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                containerMensagensDia.removeAllViews()

                for (doc in snapshot.documents) {
                    val titulo = doc.getString("titulo") ?: "(sem título)"
                    val data = doc.getString("data") ?: "(sem data)"
                    val texto = doc.getString("texto") ?: ""

                    // 💡 admin: usar card com ações
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

                    // ---------- BOTÃO EXCLUIR (opcional) ----------
                    val btnExcluir = item.findViewById<Button?>(R.id.btn_excluir)
                    btnExcluir?.setOnClickListener {
                        val scaleX = ObjectAnimator.ofFloat(btnExcluir, "scaleX", 1f, 0.9f, 1f)
                        val scaleY = ObjectAnimator.ofFloat(btnExcluir, "scaleY", 1f, 0.9f, 1f)
                        AnimatorSet().apply {
                            playTogether(scaleX, scaleY)
                            duration = 200
                            start()
                        }

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

                    // ---------- BOTÃO POSTAR (opcional) ----------
                    val idPostar = item.resources.getIdentifier("btn_postar", "id", item.context.packageName)
                    val btnPostar = if (idPostar != 0) item.findViewById<Button>(idPostar) else null
                    btnPostar?.setOnClickListener {
                        val scaleX = ObjectAnimator.ofFloat(btnPostar, "scaleX", 1f, 0.9f, 1f)
                        val scaleY = ObjectAnimator.ofFloat(btnPostar, "scaleY", 1f, 0.9f, 1f)
                        AnimatorSet().apply {
                            playTogether(scaleX, scaleY)
                            duration = 200
                            start()
                        }

                        postarMensagemDoDia(doc.getString("titulo") ?: "", doc.getString("texto") ?: "")
                    }

                    containerMensagensDia.addView(item)
                }
            }
    }

    private fun carregarLemaAno(uid: String) {
        val inflater = LayoutInflater.from(this)

        listenerLema = db.collection("lemaDoAno")
            .whereEqualTo("uid", uid)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                containerLemaAno.removeAllViews()

                for (doc in snapshot.documents) {
                    val titulo = doc.getString("titulo") ?: "(sem título)"
                    val data = doc.getString("data") ?: "(sem data)"
                    val lema = doc.getString("lema") ?: ""

                    // 💡 admin: usar card com ações
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

                    // ---------- BOTÃO EXCLUIR (opcional) ----------
                    val btnExcluir = item.findViewById<Button?>(R.id.btn_excluir)
                    btnExcluir?.setOnClickListener {
                        val scaleX = ObjectAnimator.ofFloat(btnExcluir, "scaleX", 1f, 0.9f, 1f)
                        val scaleY = ObjectAnimator.ofFloat(btnExcluir, "scaleY", 1f, 0.9f, 1f)
                        AnimatorSet().apply {
                            playTogether(scaleX, scaleY)
                            duration = 200
                            start()
                        }

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

                    // ---------- BOTÃO POSTAR (opcional) ----------
                    val idPostar = item.resources.getIdentifier("btn_postar", "id", item.context.packageName)
                    val btnPostar = if (idPostar != 0) item.findViewById<Button>(idPostar) else null
                    btnPostar?.setOnClickListener {
                        val scaleX = ObjectAnimator.ofFloat(btnPostar, "scaleX", 1f, 0.9f, 1f)
                        val scaleY = ObjectAnimator.ofFloat(btnPostar, "scaleY", 1f, 0.9f, 1f)
                        AnimatorSet().apply {
                            playTogether(scaleX, scaleY)
                            duration = 200
                            start()
                        }

                        postarLemaDoAno(titulo, lema)
                    }

                    containerLemaAno.addView(item)
                }
            }
    }

    private fun carregarAnotacoes(uid: String) {
        val inflater = LayoutInflater.from(this)

        listenerAnotacao = db.collection("anotacoes")
            .whereEqualTo("uid", uid)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                containerAnotacao.removeAllViews()
                caixaFavoritos.removeAllViews()

                for (doc in snapshot.documents) {
                    val titulo = doc.getString("titulo") ?: "(sem título)"
                    val texto = doc.getString("texto") ?: ""
                    val favoritoFirestore = doc.getBoolean("favorito") ?: false

                    val item = inflater.inflate(R.layout.card_anotacao_salva, null, false)
                    val tvTitulo = item.findViewById<TextView>(R.id.tv_titulo_anotacao_salva)
                    val tvConteudo = item.findViewById<TextView>(R.id.tv_conteudo_anotacao_salvo)
                    val estrela = item.findViewById<ImageView>(R.id.iv_estrela)
                    val btnExcluir = item.findViewById<Button>(R.id.btn_excluir)

                    tvTitulo.text = titulo
                    tvConteudo.text = texto
                    tvConteudo.visibility = View.GONE

                    var favorito = favoritoFirestore

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

                    // ---------- ESTRELA FAVORITO ----------
                    estrela.setOnClickListener {
                        val scaleX = ObjectAnimator.ofFloat(estrela, "scaleX", 1f, 0.9f, 1f)
                        val scaleY = ObjectAnimator.ofFloat(estrela, "scaleY", 1f, 0.9f, 1f)
                        AnimatorSet().apply {
                            playTogether(scaleX, scaleY)
                            duration = 200
                            start()
                        }

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
                                Toast.makeText(this, "Erro ao atualizar favorito", Toast.LENGTH_SHORT).show()
                            }

                        val parent = item.parent as? LinearLayout
                        parent?.removeView(item)

                        if (favorito) {
                            caixaFavoritos.addView(item)
                        } else {
                            containerAnotacao.addView(item, 0)
                        }

                        caixaFavoritos.visibility =
                            if (caixaFavoritos.childCount > 0) View.VISIBLE else View.GONE
                    }

                    // ---------- BOTÃO EXCLUIR ----------
                    btnExcluir.setOnClickListener {
                        val scaleX = ObjectAnimator.ofFloat(btnExcluir, "scaleX", 1f, 0.9f, 1f)
                        val scaleY = ObjectAnimator.ofFloat(btnExcluir, "scaleY", 1f, 0.9f, 1f)
                        AnimatorSet().apply {
                            playTogether(scaleX, scaleY)
                            duration = 200
                            start()
                        }

                        db.collection("anotacoes").document(doc.id)
                            .delete()
                            .addOnSuccessListener {
                                val parent = item.parent as? LinearLayout
                                parent?.removeView(item)
                                Toast.makeText(this, "Anotação excluída com sucesso!", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Erro ao excluir anotação.", Toast.LENGTH_SHORT).show()
                            }
                    }
                }

                caixaFavoritos.visibility =
                    if (caixaFavoritos.childCount > 0) View.VISIBLE else View.GONE
            }
    }

    private fun postarMensagemDoDia(titulo: String, texto: String) {
        val dataAtual = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

        val mensagemMap = hashMapOf(
            "titulo" to titulo,
            "data" to dataAtual,
            "texto" to texto,
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
                db.collection("mensagensAdmin")
                    .add(mensagemMap)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao postar mensagem do dia.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun postarLemaDoAno(titulo: String, lema: String) {
        val dataAtual = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

        val lemaMap = hashMapOf(
            "titulo" to titulo,
            "data" to dataAtual,
            "lema" to lema,
            "timestamp" to FieldValue.serverTimestamp()
        )

        db.collection("lemaAtual").document("atual")
            .set(lemaMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Lema do ano atualizado!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao postar lema do ano.", Toast.LENGTH_SHORT).show()
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
                Log.d("Pag_layouts", "Foto carregada no menu: $caminhoFoto")
            } else {
                Log.w("Pag_layouts", "Foto não encontrada no menu, usando padrão")
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
