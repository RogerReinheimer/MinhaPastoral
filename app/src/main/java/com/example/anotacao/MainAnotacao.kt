package com.example.anotacao

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.anotacao.core.SessionAuth
import com.example.anotacao.core.AdminGate
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.io.File

class MainAnotacao : AppCompatActivity() {

    private lateinit var btnBiblia: ImageView
    private lateinit var btnPagHome: ImageView
    private lateinit var btnCruz: ImageView
    private lateinit var btnMais: ImageView
    private lateinit var btnMenu: ImageView
    private lateinit var btnSalvar: AppCompatButton
    private lateinit var editTextTitulo: EditText
    private lateinit var editTextTexto: EditText

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.anotacao)

        // 🔒 Bloqueia usuários comuns — apenas admin pode acessar
        AdminGate.requireAdmin(this)

        // ---------- FINDVIEWBYID ----------
        btnBiblia = findViewById(R.id.btnMensagens7)
        btnPagHome = findViewById(R.id.btnPagHome7)
        btnCruz = findViewById(R.id.btnMensagensSemana7)
        btnMais = findViewById(R.id.btnFlutuante7)
        btnMenu = findViewById(R.id.btnMenu)
        btnSalvar = findViewById(R.id.btnSalvar)
        editTextTitulo = findViewById(R.id.editTitulo)
        editTextTexto = findViewById(R.id.editAnotacaoConteudo)

        // ---------- BOTÃO SALVAR ----------
        btnSalvar.setOnClickListener {
            val scaleX = ObjectAnimator.ofFloat(btnSalvar, "scaleX", 1f, 0.9f, 1f)
            val scaleY = ObjectAnimator.ofFloat(btnSalvar, "scaleY", 1f, 0.9f, 1f)
            AnimatorSet().apply {
                playTogether(scaleX, scaleY)
                duration = 200
                start()
            }
            salvarAnotacao()
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

        // ✅ Gate no botão “+” (item 2.4 já aplicado)
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
                        this@MainAnotacao,
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
    }

    private fun salvarAnotacao() {
        val titulo = editTextTitulo.text.toString().trim()
        val texto = editTextTexto.text.toString().trim()

        if (titulo.isEmpty() || texto.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
            return
        }

        val usuarioAtual = auth.currentUser
        if (usuarioAtual == null) {
            Toast.makeText(this, "Usuário não autenticado!", Toast.LENGTH_SHORT).show()
            return
        }

        val anotacao = hashMapOf(
            "titulo" to titulo,
            "texto" to texto,
            "uid" to usuarioAtual.uid,
            "email" to usuarioAtual.email,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("anotacoes")
            .add(anotacao)
            .addOnSuccessListener {
                Toast.makeText(this, "Anotação salva com sucesso!", Toast.LENGTH_SHORT).show()
                editTextTitulo.text.clear()
                editTextTexto.text.clear()
                startActivity(Intent(this, Pag_layouts::class.java))
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao salvar: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // --- Sheets iguais aos teus (sem mudança de regra) ---
    private fun mostrarSheetLateral() { /* igual ao teu, mantido */
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
                Log.d("MainAnotacao", "Foto carregada no menu: $caminhoFoto")
            } else {
                Log.w("MainAnotacao", "Foto não encontrada no menu, usando padrão")
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
