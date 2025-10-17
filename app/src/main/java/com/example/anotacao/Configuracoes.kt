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
import com.google.firebase.storage.FirebaseStorage




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

    companion object {
        private const val REQUEST_CODE_IMAGEM = 1002
    }

    val db = FirebaseFirestore.getInstance()



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

        // --------- CHAMAR FUNÇÃO MOSTRAR SENHA ------------
        setupPasswordVisibilityToggle(etSenha, ivToggleSenha)
        setupPasswordVisibilityToggle(etSenhaAtual, ivToggleSenhaAtual)

        // ---------- MENUS ----------
        btnMenu.setOnClickListener { mostrarSheetLateral() }
        btnFlutuante.setOnClickListener { mostrarSheetOpcoes() }

        // --------- CARREGAR DADOS USUÁRIO (NOME E FOTO EM UMA CHAMADA) --------
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        // Carrega o nome
                        val nome = document.getString("username")
                        etUsuario.setText(nome ?: "")

                        // Carrega a foto
                        val urlFoto = document.getString("fotoPerfil")
                        if (!urlFoto.isNullOrEmpty()) {
                            Glide.with(this).load(urlFoto).into(ivUsuario)
                        }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Erro ao carregar dados do usuário", Toast.LENGTH_SHORT).show()
                }
        }

        // -------- SALVAR ALTERAÇÕES -----------
        btnSalvar.setOnClickListener {
            val novoNome = etUsuario.text.toString().trim()
            val novaSenha = etSenha.text.toString()
            val senhaAtual = etSenhaAtual.text.toString()
            val usuario = FirebaseAuth.getInstance().currentUser

            // Atualizar nome no Firestore
            if (usuario != null && novoNome.isNotEmpty()) {
                val dados = hashMapOf("username" to novoNome)
                db.collection("users").document(usuario.uid).update(dados as Map<String, Any>)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Nome atualizado!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Erro ao atualizar nome.", Toast.LENGTH_SHORT).show()
                    }
            }

            // Atualizar senha com reautenticação
            val emailNaoNulo = usuario?.email
            if (novaSenha.isNotEmpty() && senhaAtual.isNotEmpty() && emailNaoNulo != null && usuario != null) {
                val credential = EmailAuthProvider.getCredential(emailNaoNulo, senhaAtual)
                usuario.reauthenticate(credential).addOnCompleteListener { authTask ->
                    if (authTask.isSuccessful) {
                        usuario.updatePassword(novaSenha).addOnCompleteListener { updateTask ->
                            if (updateTask.isSuccessful) {
                                Toast.makeText(this, "Senha atualizada com sucesso!", Toast.LENGTH_SHORT).show()
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

        txtAlterarFoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_CODE_IMAGEM)
        }
    } // fim do oncreate


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_IMAGEM && resultCode == RESULT_OK) {
            val imagemUri = data?.data
            // Verificação segura (sem o crash do !!)
            imagemUri?.let { uri ->
                ivUsuario.setImageURI(uri)
                val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
                val storageRef = FirebaseStorage.getInstance().reference.child("fotos_perfil/$uid.jpg")

                storageRef.putFile(uri)
                    .addOnSuccessListener {
                        storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                            salvarUrlFotoNoFirestore(uid, downloadUrl.toString())
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Erro ao enviar foto.", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private fun salvarUrlFotoNoFirestore(uid: String, url: String) {
        db.collection("users").document(uid)
            .update("fotoPerfil", url)
            .addOnSuccessListener {
                Toast.makeText(this, "Foto atualizada com sucesso!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao salvar URL da foto.", Toast.LENGTH_SHORT).show()
            }
    }



    // ==========================================
    // ----------- MENU LATERAL -------------
    // ==========================================
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

    // ==========================================
    // ----------- MENU DE OPÇÕES -------------
    // ==========================================
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
            // Salva a posição atual do cursor
            val selection = editText.selectionEnd

            // Verifica se a senha está visível ou oculta
            if (editText.inputType == (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                // Se estiver OCULTA, torna VISÍVEL
                editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                toggleIcon.setImageResource(R.drawable.ic_visibility) // Ícone de olho aberto
            } else {
                // Se estiver VISÍVEL, torna OCULTA
                editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                toggleIcon.setImageResource(R.drawable.ic_visibility_off) // Ícone de olho fechado
            }

            // Restaura a posição do cursor para não voltar ao início
            editText.setSelection(selection)
        }
    }
}
