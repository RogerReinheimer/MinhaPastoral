package com.example.anotacao

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.Calendar
import java.util.Random

class Pag_home : AppCompatActivity() {

    // --- Sua lógica de UI original (sem mudanças) ---
    private lateinit var tvTextVersiculoAntigo: TextView
    private lateinit var tvNumeroVersiculoAntigo: TextView
    private lateinit var tvTextVersiculoNovo: TextView
    private lateinit var tvNumeroVersiculoNovo: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pag_home)

        tvTextVersiculoAntigo = findViewById(R.id.tv_text_versiculo_antigo)
        tvNumeroVersiculoAntigo = findViewById(R.id.tv_numero_versiculo_antigo)
        tvTextVersiculoNovo = findViewById(R.id.tv_text_versiculo_novo)
        tvNumeroVersiculoNovo = findViewById(R.id.tv_numero_versiculo_novo)

        findViewById<View>(R.id.Cabecario_AT).setOnClickListener { alternarVisibilidade(R.id.Conteudo_AT) }
        findViewById<View>(R.id.Cabecario_NT).setOnClickListener { alternarVisibilidade(R.id.Conteudo_NT) }
        findViewById<View>(R.id.Cabecario_LA).setOnClickListener { alternarVisibilidade(R.id.Conteudo_LA) }
        findViewById<View>(R.id.Cabecario_MD).setOnClickListener { alternarVisibilidade(R.id.Conteudo_MD) }

        val btnMenu = findViewById<ImageView>(R.id.btnMenu)
        val btnMais = findViewById<ImageView>(R.id.btnFlutuante)
        val btnCruz = findViewById<ImageView>(R.id.btnMensagensSemana)
        val btnBiblia = findViewById<ImageView>(R.id.btnMensagens)

        btnMenu.setOnClickListener { mostrarSheetLateral() }
        btnMais.setOnClickListener { mostrarSheetOpcoes() }
        btnCruz.setOnClickListener { startActivity(Intent(this, Mensagens_semana::class.java)) }
        btnBiblia.setOnClickListener { Toast.makeText(this, "Função Bíblia ainda não implementada.", Toast.LENGTH_SHORT).show() }

        buscarVersiculosDoDia()
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


    data class ApiVerse(val pk: Int, val verse: Int, val text: String)

    data class DisplayVerse(val bookName: String, val chapter: Int, val verse: Int, val text: String)

    interface BibleApi {
        @GET("get-text/NVT/{bookId}/{chapter}/")
        suspend fun getChapter(
            @Path("bookId") bookId: Int,
            @Path("chapter") chapter: Int
        ): List<ApiVerse>
    }

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://bolls.life/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(BibleApi::class.java)

    private fun buscarVersiculosDoDia() {
        lifecycleScope.launch {
            try {
                val antigoJob = async { getVersiculoDoDiaParaTestamento("AT") }
                val novoJob = async { getVersiculoDoDiaParaTestamento("NT") }

                val antigo = antigoJob.await()
                val novo = novoJob.await()

                antigo?.let { preencherCardAntigoTestamento(it) }
                novo?.let { preencherCardNovoTestamento(it) }

            } catch (e: Exception) {
                Log.e("Pag_home", "Erro ao buscar versículos do dia: ${e.message}")
                Toast.makeText(this@Pag_home, "Falha ao carregar versículos.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private suspend fun getVersiculoDoDiaParaTestamento(testamento: String): DisplayVerse? {
        return try {
            val diaDoAno = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
            val ano = Calendar.getInstance().get(Calendar.YEAR)
            val seed = (ano * 1000 + diaDoAno).toLong()
            val random = Random(seed)

            val bookId: Int
            if (testamento == "AT") {
                bookId = random.nextInt(39) + 1
            } else {
                bookId = random.nextInt(27) + 40
            }

            val infoLivro = BibleData.mapBookIdToData[bookId] ?: return null
            val chapter = random.nextInt(infoLivro.chapters) + 1

            val capituloDaApi = api.getChapter(bookId, chapter)
            val primeiroVersiculo = capituloDaApi.firstOrNull() ?: return null


            DisplayVerse(
                bookName = infoLivro.name,
                chapter = chapter,
                verse = primeiroVersiculo.verse,
                text = primeiroVersiculo.text
            )
        } catch (e: Exception) {
            Log.e("Pag_home", "Falha ao buscar capítulo para o dia: ${e.message}")
            null
        }
    }

    private fun preencherCardAntigoTestamento(versiculo: DisplayVerse) {
        val textoLimpo = Html.fromHtml(versiculo.text, Html.FROM_HTML_MODE_LEGACY).toString()
        tvTextVersiculoAntigo.text = "\"$textoLimpo\""
        tvNumeroVersiculoAntigo.text = "${versiculo.bookName} ${versiculo.chapter}:${versiculo.verse}"
    }

    private fun preencherCardNovoTestamento(versiculo: DisplayVerse) {
        val textoLimpo = Html.fromHtml(versiculo.text, Html.FROM_HTML_MODE_LEGACY).toString()
        tvTextVersiculoNovo.text = "\"$textoLimpo\""
        tvNumeroVersiculoNovo.text = "${versiculo.bookName} ${versiculo.chapter}:${versiculo.verse}"
    }
}