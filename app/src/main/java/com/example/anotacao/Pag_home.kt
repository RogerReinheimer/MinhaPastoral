package com.example.anotacao

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Dialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
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
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.io.File
import java.util.Calendar
import java.util.Random

class Pag_home : AppCompatActivity() {

    private lateinit var tvTextVersiculoAntigo: TextView
    private lateinit var tvNumeroVersiculoAntigo: TextView
    private lateinit var tvTextVersiculoNovo: TextView
    private lateinit var tvNumeroVersiculoNovo: TextView
    private lateinit var btnMenu: ImageView
    private lateinit var btnMais: ImageView
    private lateinit var btnCruz: ImageView
    private lateinit var btnBiblia: ImageView

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://bolls.life/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(BibleApi::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)

        // ---------- VERIFICA LOGIN ----------
        val user = auth.currentUser
        if (user == null) {
            val intent = Intent(this, Pag_entrar::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        } else {
            atualizarTokenFCM(user.uid)
        }

        setContentView(R.layout.activity_pag_home)

        // ---------- FINDVIEWBYID ----------
        tvTextVersiculoAntigo = findViewById(R.id.tv_text_versiculo_antigo)
        tvNumeroVersiculoAntigo = findViewById(R.id.tv_numero_versiculo_antigo)
        tvTextVersiculoNovo = findViewById(R.id.tv_text_versiculo_novo)
        tvNumeroVersiculoNovo = findViewById(R.id.tv_numero_versiculo_novo)
        btnMenu = findViewById(R.id.btnMenu)
        btnMais = findViewById(R.id.btnFlutuante)
        btnCruz = findViewById(R.id.btnMensagensSemana)
        btnBiblia = findViewById(R.id.btnMensagens)

        // ---------- CABEÇÁRIOS EXPANSÍVEIS ----------
        findViewById<View>(R.id.Cabecario_AT).setOnClickListener { alternarVisibilidade(R.id.Conteudo_AT) }
        findViewById<View>(R.id.Cabecario_NT).setOnClickListener { alternarVisibilidade(R.id.Conteudo_NT) }
        findViewById<View>(R.id.Cabecario_LA).setOnClickListener { alternarVisibilidade(R.id.Conteudo_LA) }
        findViewById<View>(R.id.Cabecario_MD).setOnClickListener { alternarVisibilidade(R.id.Conteudo_MD) }

        // ---------- BOTÕES DE NAVEGAÇÃO (300ms, 0.8) ----------
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

        btnMais.setOnClickListener {
            val scaleX = ObjectAnimator.ofFloat(btnMais, "scaleX", 1f, 0.8f, 1f)
            val scaleY = ObjectAnimator.ofFloat(btnMais, "scaleY", 1f, 0.8f, 1f)
            AnimatorSet().apply {
                playTogether(scaleX, scaleY)
                duration = 300
                start()
            }
            mostrarSheetOpcoes()
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

        // ---------- CARREGAR CONTEÚDO ----------
        buscarVersiculosDoDia()
        carregarMensagemDoDia()
        carregarLemaDoAno()
        monitorarNovaMensagem(user.uid)

        // ---------- PERMISSÃO NOTIFICAÇÕES ----------
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001) {
            if (grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissão concedida!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permissão negada. Notificações não funcionarão.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun atualizarTokenFCM(uid: String) {
        com.google.firebase.messaging.FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                db.collection("users")
                    .document(uid)
                    .update("fcmToken", token)
                    .addOnSuccessListener {
                        Log.d("Pag_home", "Token FCM atualizado com sucesso")
                    }
                    .addOnFailureListener { e ->
                        Log.e("Pag_home", "Erro ao atualizar token FCM: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                Log.e("Pag_home", "Falha ao pegar token FCM: ${e.message}")
            }
    }

    private fun enviarNotificacao(title: String, body: String) {
        val channelId = "canal_geral"
        val notificationId = 100

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Canal Geral"
            val descriptionText = "Canal para notificações gerais"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, Pag_home::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(this)) {
            notify(notificationId, builder.build())
        }
    }

    private fun monitorarNovaMensagem(uid: String) {
        db.collection("mensagemDoDia")
            .document("atual")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("Pag_home", "Erro ao monitorar mensagem do dia: ${e.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val titulo = snapshot.getString("titulo") ?: "Nova mensagem"
                    val texto = snapshot.getString("texto") ?: ""
                    enviarNotificacao(titulo, texto)
                }
            }
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
                Log.d("Pag_home", "Foto carregada no menu: $caminhoFoto")
            } else {
                Log.w("Pag_home", "Foto não encontrada no menu, usando padrão")
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

    private fun carregarLemaDoAno() {
        val tvConteudo = findViewById<TextView>(R.id.Conteudo_LA)

        db.collection("lemaAtual")
            .document("atual")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("Pag_home", "Erro ao buscar lema do ano: ${e.message}")
                    Toast.makeText(this, "Falha ao carregar lema do ano.", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val lema = snapshot.getString("lema") ?: "Nenhum lema disponível."
                    tvConteudo.text = lema
                } else {
                    tvConteudo.text = "Lema do ano não definido."
                }
            }
    }

    private fun carregarMensagemDoDia() {
        val tvTitulo = findViewById<TextView>(R.id.tvSubtitulo)
        val tvData = findViewById<TextView>(R.id.tvData)
        val tvTexto = findViewById<TextView>(R.id.Conteudo_MD)
        val cabecario = findViewById<LinearLayout>(R.id.Cabecario_MD)

        db.collection("mensagemDoDia")
            .document("atual")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("Pag_home", "Erro ao buscar mensagem do dia: ${e.message}")
                    Toast.makeText(this, "Falha ao carregar mensagem do dia.", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val titulo = snapshot.getString("titulo") ?: "Sem título"
                    val data = snapshot.getString("data") ?: "--/--/----"
                    val texto = snapshot.getString("texto") ?: "Nenhum texto disponível."

                    tvTitulo.text = titulo
                    tvData.text = data
                    tvTexto.text = texto
                    tvTexto.visibility = View.VISIBLE

                    cabecario.setOnClickListener {
                        tvTexto.visibility =
                            if (tvTexto.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                    }
                } else {
                    tvTitulo.text = "Sem mensagem do dia"
                    tvData.text = ""
                    tvTexto.text = ""
                    tvTexto.visibility = View.GONE
                }
            }
    }

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

            val bookId = if (testamento == "AT") {
                random.nextInt(39) + 1
            } else {
                random.nextInt(27) + 40
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

    // ---------- DATA CLASSES E INTERFACE ----------
    data class ApiVerse(val pk: Int, val verse: Int, val text: String)

    data class DisplayVerse(val bookName: String, val chapter: Int, val verse: Int, val text: String)

    interface BibleApi {
        @GET("get-text/NVT/{bookId}/{chapter}/")
        suspend fun getChapter(
            @Path("bookId") bookId: Int,
            @Path("chapter") chapter: Int
        ): List<ApiVerse>
    }
}
