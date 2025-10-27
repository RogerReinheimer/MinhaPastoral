package com.example.anotacao

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
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
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Calendar

class PredefinicaoMsg : AppCompatActivity() {

    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_predefinicao_msg)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnBiblia = findViewById<ImageView>(R.id.btnMensagens6)
        val btnPagHome = findViewById<ImageView>(R.id.btnPagHome6)
        val btnCruz = findViewById<ImageView>(R.id.btnMensagensSemana6)
        val btnMais = findViewById<ImageView>(R.id.btnFlutuante6)
        val btnMenu = findViewById<ImageView>(R.id.btnMenu)
        val btnSalvar = findViewById<AppCompatButton>(R.id.btnSalvar)

        // note: IDs in the layout
        val editTextTitulo = findViewById<EditText>(R.id.editTitulo1)
        val editTextData = findViewById<EditText>(R.id.editData)
        val editTextMensagem = findViewById<EditText>(R.id.editMensagem) // this EditText holds the message content in layout

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

        // ----------- BOTÃO SALVAR (AGORA COM FIRESTORE) -------------
        btnSalvar.setOnClickListener {
            val titulo = editTextTitulo.text.toString().trim()
            val data = editTextData.text.toString().trim()
            val texto = editTextMensagem.text.toString().trim()

            if (titulo.isEmpty() || data.isEmpty() || texto.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val usuarioAtual = auth.currentUser
            if (usuarioAtual == null) {
                Toast.makeText(this, "Usuário não autenticado!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
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

                    // volta pra tela de layouts (se quiser que abra)
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
        btnBiblia.setOnClickListener {
            val intent = Intent(this, Mensagens::class.java)
            startActivity(intent)
        }
        btnPagHome.setOnClickListener {
            val intent = Intent(this, Pag_home::class.java)
            startActivity(intent)
        }
        btnCruz.setOnClickListener {
            val intent = Intent(this, Mensagens_semana::class.java)
            startActivity(intent)
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

        // ======== ATUALIZA FOTO DE PERFIL E NOME ========
        val imgPerfil = dialog.findViewById<ImageView>(R.id.imgPerfil)
        val txtPerfilNome = dialog.findViewById<TextView>(R.id.txtPerfilNome)

        // Carrega nome do Firestore
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val nome = document.getString("username")
                        txtPerfilNome.text = "Olá, ${nome ?: "usuário"}!"
                    }
                }
        }

        // Carrega imagem local salva
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val caminhoFoto = prefs.getString("foto_local", null)
        if (caminhoFoto != null) {
            val file = java.io.File(caminhoFoto)
            if (file.exists()) {
                Glide.with(this)
                    .load(android.net.Uri.fromFile(file))
                    .circleCrop()
                    .into(imgPerfil)
            } else {
                imgPerfil.setImageResource(R.drawable.img_3) // imagem padrão
            }
        } else {
            imgPerfil.setImageResource(R.drawable.img_3)
        }

        // ======== AÇÕES DOS BOTÕES ========
        val opcLayout = dialog.findViewById<LinearLayout>(R.id.layoutLayout)
        opcLayout.setOnClickListener {
            val intent = Intent(this, Pag_layouts::class.java)
            startActivity(intent)
            dialog.dismiss()
        }

        val opcConfig = dialog.findViewById<LinearLayout>(R.id.layoutConfig)
        opcConfig.setOnClickListener {
            val intent = Intent(this, Configuracoes::class.java)
            startActivity(intent)
            dialog.dismiss()
        }

        val opcSair = dialog.findViewById<LinearLayout>(R.id.layoutSair)
        opcSair.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, Pag_entrar::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            dialog.dismiss()
            Toast.makeText(this, "Você saiu da conta.", Toast.LENGTH_SHORT).show()
        }

        dialog.show()
    }


    // ----------- MENU DE OPÇÕES (BOTÃO +) -------------
    private fun mostrarSheetOpcoes() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_opcoes, null)

        val opcaoAnotacao = view.findViewById<TextView>(R.id.opcaoAnotacao)
        val opcaoLema = view.findViewById<TextView>(R.id.opcaoLema)
        val opcaoMensagem = view.findViewById<TextView>(R.id.opcaoMensagem)

        opcaoAnotacao.setOnClickListener {
            startActivity(Intent(this, MainAnotacao::class.java))
            bottomSheetDialog.dismiss()
        }

        opcaoLema.setOnClickListener {
            startActivity(Intent(this, PredefinicaoLema::class.java))
            bottomSheetDialog.dismiss()
        }

        opcaoMensagem.setOnClickListener {
            startActivity(Intent(this, PredefinicaoMsg::class.java))
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.show()
    }
}
