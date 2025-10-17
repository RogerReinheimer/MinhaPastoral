/*package com.example.anotacao

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.database.FirebaseDatabase

class Configuracoes : AppCompatActivity() {

    private lateinit var ivUsuario: ImageView
    private lateinit var txtAlterarFoto: TextView
    private lateinit var etUsuario: EditText
    private lateinit var etSenha: EditText
    private lateinit var cbNotificacoes: CheckBox
    private lateinit var btnSalvar: Button
    private lateinit var btnMenu: ImageView
    private lateinit var btnFlutuante: ImageView

    private var imageUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 1001

    private val auth = FirebaseAuth.getInstance()
    private val user = auth.currentUser
    private val database = FirebaseDatabase.getInstance().reference
    private val storage = FirebaseStorage.getInstance().reference

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

        // ---------- CARREGAR DADOS DO FIREBASE ----------
        carregarDadosUsuario()

        // ---------- ALTERAR FOTO ----------
        txtAlterarFoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        // ---------- SALVAR ALTERAÇÕES ----------
        btnSalvar.setOnClickListener { salvarAlteracoes() }

        // ---------- MENUS ----------
        btnMenu.setOnClickListener { mostrarSheetLateral() }
        btnFlutuante.setOnClickListener { mostrarSheetOpcoes() }
    }

    // ==========================================
    // CARREGAR DADOS DO FIREBASE
    // ==========================================
    private fun carregarDadosUsuario() {
        user?.uid?.let { uid ->
            database.child("usuarios").child(uid).get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val nome = snapshot.child("nome").value?.toString() ?: ""
                    val notificacoes = snapshot.child("notificacoes").value?.toString()?.toBoolean() ?: false
                    val fotoUrl = snapshot.child("fotoUrl").value?.toString() ?: ""

                    etUsuario.setText(nome)
                    cbNotificacoes.isChecked = notificacoes

                    if (fotoUrl.isNotEmpty()) {
                        Picasso.get().load(fotoUrl).into(ivUsuario)
                    }
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Erro ao carregar dados.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ==========================================
    // SALVAR ALTERAÇÕES
    // ==========================================
    private fun salvarAlteracoes() {
        val novoNome = etUsuario.text.toString().trim()
        val novaSenha = etSenha.text.toString().trim()
        val notificacoes = cbNotificacoes.isChecked

        if (novoNome.isEmpty()) {
            Toast.makeText(this, "Digite o nome de usuário.", Toast.LENGTH_SHORT).show()
            return
        }

        user?.uid?.let { uid ->
            val userRef = database.child("usuarios").child(uid)

            // Atualiza nome e notificações
            val updates = mapOf(
                "nome" to novoNome,
                "notificacoes" to notificacoes
            )

            userRef.updateChildren(updates).addOnSuccessListener {
                // Atualiza senha se o campo não estiver vazio
                if (novaSenha.isNotEmpty()) {
                    user?.let { usr ->
                        usr.updatePassword(novaSenha).addOnSuccessListener {
                            Toast.makeText(this, "Senha atualizada.", Toast.LENGTH_SHORT).show()
                        }.addOnFailureListener {
                            Toast.makeText(this, "Erro ao atualizar senha.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                // Upload da foto (se houver)
                if (imageUri != null) {
                    val storageRef = storage.child("fotos_perfil/$uid.jpg")
                    storageRef.putFile(imageUri!!)
                        .addOnSuccessListener {
                            storageRef.downloadUrl.addOnSuccessListener { uri ->
                                userRef.child("fotoUrl").setValue(uri.toString())
                                Toast.makeText(this, "Dados atualizados com sucesso!", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Erro ao enviar imagem.", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "Dados atualizados com sucesso!", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Erro ao salvar alterações.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ==========================================
    // RESULTADO AO SELECIONAR IMAGEM
    // ==========================================
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.data
            ivUsuario.setImageURI(imageUri)
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
}
*/