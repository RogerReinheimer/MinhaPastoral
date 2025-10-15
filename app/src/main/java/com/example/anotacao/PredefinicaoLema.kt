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
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Calendar

class PredefinicaoLema : AppCompatActivity() {

    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
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
        btnCruz.setOnClickListener { startActivity(Intent(this, Mensagens_semana::class.java)) }
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
