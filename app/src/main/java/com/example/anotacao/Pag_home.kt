package com.example.anotacao

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.lifecycle.lifecycleScope


class Pag_home : AppCompatActivity() {

    private lateinit var tvTextVersiculoAntigo: TextView
    private lateinit var tvNumeroVersiculoAntigo: TextView
    private lateinit var tvTextVersiculoNovo: TextView
    private lateinit var tvNumeroVersiculoNovo: TextView

    private val livrosExcluidos = setOf(
        "Tobias", "Judite", "Sabedoria", "Eclesiástico", "Baruque", "1 Macabeus", "2 Macabeus"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pag_home)

        tvTextVersiculoAntigo = findViewById(R.id.tv_text_versiculo_antigo)
        tvNumeroVersiculoAntigo = findViewById(R.id.tv_numero_versiculo_antigo)
        tvTextVersiculoNovo = findViewById(R.id.tv_text_versiculo_novo)
        tvNumeroVersiculoNovo = findViewById(R.id.tv_numero_versiculo_novo)

        // Exemplo simples de expandir seções (ajuste conforme seu layout)
        findViewById<View>(R.id.Cabecario_AT).setOnClickListener {
            alternarVisibilidade(R.id.Conteudo_AT)
        }
        findViewById<View>(R.id.Cabecario_NT).setOnClickListener {
            alternarVisibilidade(R.id.Conteudo_NT)
        }

        val btnMenu = findViewById<ImageView>(R.id.btnMenu)
        val btnMais = findViewById<ImageView>(R.id.btnFlutuante)
        val btnCruz = findViewById<ImageView>(R.id.btnMensagensSemana)
        val btnBiblia = findViewById<ImageView>(R.id.btnMensagens)

        btnMenu.setOnClickListener { mostrarSheetLateral() }
        btnMais.setOnClickListener { mostrarSheetOpcoes() }

        btnCruz.setOnClickListener {
            startActivity(Intent(this, Mensagens_semana::class.java))
        }

        btnBiblia.setOnClickListener {
            Toast.makeText(this, "Função Bíblia ainda não implementada.", Toast.LENGTH_SHORT).show()
        }

        buscarVersiculos()
    }

    private fun alternarVisibilidade(id: Int) {
        val view = findViewById<View>(id)
        view.visibility = if (view.visibility == View.VISIBLE) View.GONE else View.VISIBLE
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

        val opcLayout = dialog.findViewById<LinearLayout>(R.id.layoutLayout)
        opcLayout.setOnClickListener {
            val intent = Intent(this, Pag_layouts::class.java)
            startActivity(intent)
            dialog.dismiss()
        }

        dialog.show()
    }

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

    // --- LÓGICA DE BUSCA DE VERSÍCULOS (API BOLLS) ---
    private fun buscarVersiculos() {
        lifecycleScope.launch {
            try {
                val antigoTestamentoJob = async { buscarVersiculoValido("OT") }
                val novoTestamentoJob = async { buscarVersiculoValido("NT") }

                val versiculoAntigo = antigoTestamentoJob.await()
                val versiculoNovo = novoTestamentoJob.await()

                versiculoAntigo?.let { preencherCardAntigoTestamento(it) }
                versiculoNovo?.let { preencherCardNovoTestamento(it) }

            } catch (e: Exception) {
                Log.e("Pag_home", "Erro ao buscar versículos: ${e.message}")
                Toast.makeText(this@Pag_home, "Falha ao carregar versículos.", Toast.LENGTH_LONG).show()
            }
        }
    }

    // ATENÇÃO: A função agora retorna 'ApiResponse?'
    private suspend fun buscarVersiculoValido(testamentoDesejado: String): ApiResponse? {
        while (true) {
            try {
                val respostaApi = RetrofitClient.apiService.getVersiculoAleatorio("KJV")
                val versiculo = respostaApi.firstOrNull() ?: continue

                val testamentoCorreto = versiculo.reference.book.testament == testamentoDesejado
                val livroValido = versiculo.reference.book.name !in livrosExcluidos

                if (testamentoCorreto && livroValido) {
                    return versiculo
                }

            } catch (e: Exception) {
                Log.e("Pag_home", "Falha em uma tentativa de busca: ${e.message}")
                return null
            }
        }
    }

    // ATENÇÃO: O parâmetro agora é do tipo 'ApiResponse'
    private fun preencherCardAntigoTestamento(versiculo: ApiResponse) {
        tvTextVersiculoAntigo.text = "\"${versiculo.text}\""
        tvNumeroVersiculoAntigo.text = "${versiculo.reference.book.name} ${versiculo.reference.chapter}:${versiculo.reference.verse}"
    }

    // ATENÇÃO: O parâmetro agora é do tipo 'ApiResponse'
    private fun preencherCardNovoTestamento(versiculo: ApiResponse) {
        tvTextVersiculoNovo.text = "\"${versiculo.text}\""
        tvNumeroVersiculoNovo.text = "${versiculo.reference.book.name} ${versiculo.reference.chapter}:${versiculo.reference.verse}"
    }
}
