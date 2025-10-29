package com.example.anotacao

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.ContentValues.TAG
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
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.io.File
import java.util.Calendar

class PredefinicaoLema : AppCompatActivity() {

    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_predefinicao_lema)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnBiblia = findViewById<ImageView>(R.id.btnMensagens5)
        val btnPagHome = findViewById<ImageView>(R.id.btnPagHome5)
        val btnCruz = findViewById<ImageView>(R.id.btnMensagensSemana5)
        val btnMais = findViewById<ImageView>(R.id.btnFlutuante5)
        val btnMenu = findViewById<ImageView>(R.id.btnMenu)
        val btnSalvar = findViewById<AppCompatButton>(R.id.btnSalvar)

        val editTextTitulo = findViewById<EditText>(R.id.etTituloLema)
        val editTextData = findViewById<EditText>(R.id.etDataLema)
        val editTextLema = findViewById<EditText>(R.id.etTextoLema) // aqui o lema

        // ----------- SELECIONAR DATA -------------
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

        // ----------- BOTÃO SALVAR -------------
        btnSalvar.setOnClickListener {
            val titulo = editTextTitulo.text.toString().trim()
            val data = editTextData.text.toString().trim()
            val lema = editTextLema.text.toString().trim()

            if (titulo.isEmpty() || data.isEmpty() || lema.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val usuarioAtual = auth.currentUser
            if (usuarioAtual == null) {
                Toast.makeText(this, "Usuário não autenticado!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val documentoLema = hashMapOf(
                "titulo" to titulo,
                "data" to data,
                "lema" to lema,
                "uid" to usuarioAtual.uid,
                "email" to usuarioAtual.email,
                "timestamp" to System.currentTimeMillis()
            )

            db.collection("lemaDoAno")
                .add(documentoLema)
                .addOnSuccessListener {
                    Toast.makeText(this, "Lema salvo com sucesso!", Toast.LENGTH_SHORT).show()
                    editTextTitulo.text.clear()
                    editTextData.text.clear()
                    editTextLema.text.clear()

                    val intent = Intent(this, Pag_layouts::class.java)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Erro ao salvar: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }

        // ----------- NAVEGAÇÕES -------------
        btnMais.setOnClickListener { mostrarSheetOpcoes() }
        btnMenu.setOnClickListener { mostrarSheetLateral() }
        btnBiblia.setOnClickListener { startActivity(Intent(this, Mensagens::class.java)) }
        btnPagHome.setOnClickListener { startActivity(Intent(this, Pag_home::class.java)) }
        btnCruz.setOnClickListener { startActivity(Intent(this, Mensagens_historico::class.java)) }
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


    // ----------- MENU DE OPÇÕES -------------
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
