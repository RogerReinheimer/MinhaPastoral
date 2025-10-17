package com.example.anotacao

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class Mensagens : AppCompatActivity() {

    private var isExpanded2 = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mensagens)

        val Cabecario_MDI = findViewById<View>(R.id.Cabecario_MDI)
        val Conteudo_MDI = findViewById<View>(R.id.Conteudo_MDI)

        Cabecario_MDI.setOnClickListener {
            if (isExpanded2) {
                Conteudo_MDI.visibility = View.GONE
            } else {
                Conteudo_MDI.visibility = View.VISIBLE
            }
            isExpanded2 = !isExpanded2
        }

        val btnHome = findViewById<ImageView>(R.id.btnPagHome1)
        val btnCruz = findViewById<ImageView>(R.id.btnMensagensSemana1)

        btnHome.setOnClickListener {
            val intent = Intent(this, Pag_home::class.java)
            startActivity(intent)
        }

        btnCruz.setOnClickListener {
            val intent = Intent(this, Mensagens_semana::class.java)
            startActivity(intent)
        }

        //botao mais

        val btnMais = findViewById<ImageView>(R.id.btnFlutuante)
        btnMais.setOnClickListener {
            mostrarSheetOpcoes()
        }

        val btnMenu = findViewById<ImageView>(R.id.btnMenu)
        btnMenu.setOnClickListener {
            mostrarSheetLateral()
        }

    }//oncreate

    private fun mostrarSheetLateral() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.bottom_sheet_layout)

        // --- INÍCIO DA LÓGICA PARA BUSCAR O NOME ---

        // 1. Pega a referência do TextView dentro do layout do Dialog
        val txtPerfilNome = dialog.findViewById<TextView>(R.id.txtPerfilNome)

        // 2. Pega as instâncias do Firebase Auth e Firestore
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        val usuarioAtual = auth.currentUser

        // 3. Verifica se tem um usuário logado
        if (usuarioAtual != null) {
            val userId = usuarioAtual.uid
            val docRef = db.collection("usuarios").document(userId) // Assumindo coleção "usuarios"

            // 4. Busca o documento no Firestore
            docRef.get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val nome = document.getString("nome") // Assumindo campo "nome"
                        if (nome != null) {
                            // 5. Atualiza o texto do TextView
                            txtPerfilNome.text = "Olá, $nome!"
                        } else {
                            txtPerfilNome.text = "Olá, Visitante!"
                        }
                    } else {
                        txtPerfilNome.text = "Olá, Visitante!"
                    }
                }
                .addOnFailureListener { exception ->
                    // Em caso de erro, mostra uma mensagem e um texto padrão
                    Toast.makeText(this, "Erro ao buscar dados.", Toast.LENGTH_SHORT).show()
                    txtPerfilNome.text = "Olá!"
                }
        } else {
            // Caso não haja usuário logado
            txtPerfilNome.text = "Olá, Visitante!"
        }

        // --- FIM DA LÓGICA PARA BUSCAR O NOME ---


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
        /*val opcConfig = dialog.findViewById<LinearLayout>(R.id.layoutConfig)
        opcConfig.setOnClickListener {
            val intent = Intent(this, Configuracoes::class.java)
            startActivity(intent)
            dialog.dismiss()
        }*/

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

    //função opções
    private fun mostrarSheetOpcoes() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_opcoes, null)

        val opcaoAnotacao = view.findViewById<TextView>(R.id.opcaoAnotacao)
        val opcaoLema = view.findViewById<TextView>(R.id.opcaoLema)
        val opcaoMensagem = view.findViewById<TextView>(R.id.opcaoMensagem)

        opcaoAnotacao.setOnClickListener {
            //redirecionar
            val intent = Intent(this, MainAnotacao::class.java)
            startActivity(intent)
            // ação para Adicionar Anotação
            bottomSheetDialog.dismiss()
        }

        opcaoLema.setOnClickListener {
            //redirecionar
            val intent = Intent(this, PredefinicaoLema::class.java)
            startActivity(intent)
            // ação para Adicionar Lema
            bottomSheetDialog.dismiss()
        }

        opcaoMensagem.setOnClickListener {
            //redirecionar
            val intent = Intent(this, PredefinicaoMsg::class.java)
            startActivity(intent)
            // ação para Adicionar Mensagem
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.show()
    }

}//fim da class