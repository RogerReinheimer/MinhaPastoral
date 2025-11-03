package com.example.anotacao

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.DatePickerDialog
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
import java.util.Calendar

class PredefinicaoMsg : AppCompatActivity() {

    private lateinit var btnBiblia: ImageView
    private lateinit var btnPagHome: ImageView
    private lateinit var btnCruz: ImageView
    private lateinit var btnMais: ImageView
    private lateinit var btnMenu: ImageView
    private lateinit var btnSalvar: AppCompatButton
    private lateinit var editTextTitulo: EditText
    private lateinit var editTextData: EditText
    private lateinit var editTextMensagem: EditText

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_predefinicao_msg)

        // 🔒 Tela exclusiva de admin
        AdminGate.requireAdmin(this)

        // ---------- FINDVIEWBYID ----------
        btnBiblia = findViewById(R.id.btnMensagens6)
        btnPagHome = findViewById(R.id.btnPagHome6)
        btnCruz = findViewById(R.id.btnMensagensSemana6)
        btnMais = findViewById(R.id.btnFlutuante6)
        btnMenu = findViewById(R.id.btnMenu)
        btnSalvar = findViewById(R.id.btnSalvar)
        editTextTitulo = findViewById(R.id.editTitulo1)
        editTextData = findViewById(R.id.editData)
        editTextMensagem = findViewById(R.id.editMensagem)

        // ---------- SELECIONAR DATA ----------
        editTextData.setOnClickListener {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val dateText = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                    editTextData.setText(dateText)
                },
                year, month, day
            )
            datePickerDialog.show()
        }

        // ---------- BOTÃO SALVAR ----------
        btnSalvar.setOnClickListener {
            val scaleX = ObjectAnimator.ofFloat(btnSalvar, "scaleX", 1f, 0.9f, 1f)
            val scaleY = ObjectAnimator.ofFloat(btnSalvar, "scaleY", 1f, 0.9f, 1f)
            AnimatorSet().apply {
                playTogether(scaleX, scaleY)
                duration = 200
                start()
            }
            salvarMensagem()
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

        // ✅ Gate no botão “+”
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
                        this@PredefinicaoMsg,
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

    private fun salvarMensagem() {
        val titulo = editTextTitulo.text.toString().trim()
        val data = editTextData.text.toString().trim()
        val texto = editTextMensagem.text.toString().trim()

        if (titulo.isEmpty() || data.isEmpty() || texto.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
            return
        }

        val usuarioAtual = auth.currentUser
        if (usuarioAtual == null) {
            Toast.makeText(this, "Usuário não autenticado!", Toast.LENGTH_SHORT).show()
            return
        }

        val mensagem = hashMapOf(
            "titulo" to titulo,
            "data" to data,
            "texto" to texto,
            "uid" to usuarioAtual.uid,
            "email" to usuarioAtual.email,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("mensagens")
            .add(mensagem)
            .addOnSuccessListener {
                Toast.makeText(this, "Mensagem salva com sucesso!", Toast.LENGTH_SHORT).show()
                editTextTitulo.text.clear()
                editTextData.text.clear()
                editTextMensagem.text.clear()
                startActivity(Intent(this, Pag_layouts::class.java))
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao salvar: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // --- Sheet lateral ---
    private fun mostrarSheetLateral() { /* (igual ao teu) */
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
                Log.d("PredefinicaoMsg", "Foto carregada no menu: $caminhoFoto")
            } else {
                Log.w("PredefinicaoMsg", "Foto não encontrada no menu, usando padrão")
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
