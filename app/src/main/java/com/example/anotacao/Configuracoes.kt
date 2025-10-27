package com.example.anotacao

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class Configuracoes : AppCompatActivity() {

    private lateinit var ivUsuario: ImageView
    private lateinit var txtAlterarFoto: TextView
    private lateinit var etUsuario: EditText
    private lateinit var etSenha: EditText
    private lateinit var cbNotificacoes: CheckBox
    private lateinit var btnSalvar: Button
    private lateinit var btnMenu: ImageView
    private lateinit var btnFlutuante: ImageView
    private lateinit var etSenhaAtual: EditText
    private lateinit var ivToggleSenha: ImageView
    private lateinit var ivToggleSenhaAtual: ImageView
    private lateinit var btnBiblia: ImageView
    private lateinit var btnPagHome: ImageView
    private lateinit var btnCruz: ImageView

    private val db = FirebaseFirestore.getInstance()

    companion object {
        private const val REQUEST_CODE_IMAGEM = 1002
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configuracoes)

        // ---------- FINDVIEWBYID ----------
        ivUsuario = findViewById(R.id.ivUsuario)
        txtAlterarFoto = findViewById(R.id.txtAlterarFoto)
        etUsuario = findViewById(R.id.etUsuario)
        etSenha = findViewById(R.id.etSenha)
        cbNotificacoes = findViewById(R.id.cbNotificacoes)
        btnSalvar = findViewById(R.id.btnSalvar)
        btnMenu = findViewById(R.id.btnMenu)
        btnFlutuante = findViewById(R.id.btnFlutuante)
        etSenhaAtual = findViewById(R.id.etSenhaAtual)
        ivToggleSenha = findViewById(R.id.ivToggleSenha)
        ivToggleSenhaAtual = findViewById(R.id.ivToggleSenhaAtual)
        btnBiblia = findViewById(R.id.btnBiblia)
        btnPagHome = findViewById(R.id.btnHome)
        btnCruz = findViewById(R.id.btnCruz)

        // --------- CHAMAR FUNÇÃO MOSTRAR SENHA ------------
        setupPasswordVisibilityToggle(etSenha, ivToggleSenha)
        setupPasswordVisibilityToggle(etSenhaAtual, ivToggleSenhaAtual)

        // ---------- MENUS ----------
        btnMenu.setOnClickListener { mostrarSheetLateral() }
        btnFlutuante.setOnClickListener { mostrarSheetOpcoes() }
        btnBiblia.setOnClickListener {
            startActivity(Intent(this, Mensagens::class.java))
        }
        btnPagHome.setOnClickListener {
            startActivity(Intent(this, Pag_home::class.java))
        }
        btnCruz.setOnClickListener {
            startActivity(Intent(this, Mensagens_semana::class.java))
        }

        // --------- CARREGAR DADOS USUÁRIO --------
        val usuario = FirebaseAuth.getInstance().currentUser
        val uid = usuario?.uid

        if (uid != null) {
            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        etUsuario.setText(document.getString("username") ?: "")
                        val urlFoto = document.getString("fotoPerfil")
                        if (!urlFoto.isNullOrEmpty()) {
                            Glide.with(this)
                                .load(urlFoto)
                                .skipMemoryCache(true)
                                .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
                                .circleCrop()
                                .into(ivUsuario)
                        }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Erro ao carregar dados do usuário", Toast.LENGTH_SHORT).show()
                }
        }

        // Carrega foto local se existir
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val caminhoFoto = prefs.getString("foto_local", null)
        if (caminhoFoto != null) {
            val file = java.io.File(caminhoFoto)
            if (file.exists()) {
                Glide.with(this)
                    .load(android.net.Uri.fromFile(file))
                    .circleCrop()
                    .into(ivUsuario)
            }
        }

        // -------- SALVAR ALTERAÇÕES -----------
        btnSalvar.setOnClickListener {
            val novoNome = etUsuario.text.toString().trim()
            val novaSenha = etSenha.text.toString()
            val senhaAtual = etSenhaAtual.text.toString()

            // Atualizar nome
            if (usuario != null && novoNome.isNotEmpty()) {
                db.collection("users").document(usuario.uid)
                    .update("username", novoNome)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Nome atualizado!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Erro ao atualizar nome.", Toast.LENGTH_SHORT).show()
                    }
            }

            // Atualizar senha
            val email = usuario?.email
            if (!novaSenha.isNullOrEmpty() && !senhaAtual.isNullOrEmpty() && !email.isNullOrEmpty()) {
                val credential = EmailAuthProvider.getCredential(email, senhaAtual)
                usuario.reauthenticate(credential).addOnCompleteListener { authTask ->
                    if (authTask.isSuccessful) {
                        usuario.updatePassword(novaSenha).addOnCompleteListener { updateTask ->
                            if (updateTask.isSuccessful) {
                                Toast.makeText(this, "Senha atualizada!", Toast.LENGTH_SHORT).show()
                                etSenha.text.clear()
                                etSenhaAtual.text.clear()
                            } else {
                                Toast.makeText(this, "Erro ao atualizar senha.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(this, "Senha atual incorreta.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // -------- ALTERAR FOTO --------
        txtAlterarFoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_CODE_IMAGEM)
        }

        // -------- NOTIFICAÇÕES --------
        cbNotificacoes.isChecked = prefs.getBoolean("receber_notificacoes", false)
        cbNotificacoes.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("receber_notificacoes", isChecked).apply()

            if (isChecked) {
                FirebaseMessaging.getInstance().subscribeToTopic("geral")
                    .addOnCompleteListener {
                        Toast.makeText(this, "Notificações ativadas!", Toast.LENGTH_SHORT).show()
                    }
                uid?.let { id ->
                    db.collection("users").document(id).update("notificacoes", true)
                }
            } else {
                FirebaseMessaging.getInstance().unsubscribeFromTopic("geral")
                    .addOnCompleteListener {
                        Toast.makeText(this, "Notificações desativadas.", Toast.LENGTH_SHORT).show()
                    }
                uid?.let { id ->
                    db.collection("users").document(id).update("notificacoes", false)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_IMAGEM && resultCode == RESULT_OK) {
            val imagemUri = data?.data
            imagemUri?.let { uri ->
                Glide.with(this)
                    .load(uri)
                    .circleCrop()
                    .into(ivUsuario)
                salvarImagemLocal(uri)
            }
        }
    }

    private fun salvarImagemLocal(uri: android.net.Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val file = java.io.File(filesDir, "foto_perfil.jpg")
            val outputStream = java.io.FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()

            val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
            prefs.edit().putString("foto_local", file.absolutePath).apply()

            Toast.makeText(this, "Foto salva localmente!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Erro ao salvar foto local.", Toast.LENGTH_SHORT).show()
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

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    txtPerfilNome.text = "Olá, ${document.getString("username") ?: "usuário"}!"
                }
        }

        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val caminhoFoto = prefs.getString("foto_local", null)
        if (caminhoFoto != null && java.io.File(caminhoFoto).exists()) {
            Glide.with(this)
                .load(android.net.Uri.fromFile(java.io.File(caminhoFoto)))
                .circleCrop()
                .into(imgPerfil)
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
            FirebaseAuth.getInstance().signOut()
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

    private fun setupPasswordVisibilityToggle(editText: EditText, toggleIcon: ImageView) {
        toggleIcon.setOnClickListener {
            val selection = editText.selectionEnd
            if (editText.inputType == (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                toggleIcon.setImageResource(R.drawable.ic_visibility)
            } else {
                editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                toggleIcon.setImageResource(R.drawable.ic_visibility_off)
            }
            editText.setSelection(selection)
        }
    }
}
