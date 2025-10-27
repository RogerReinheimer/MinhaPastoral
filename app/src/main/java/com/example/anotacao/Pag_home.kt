package com.example.anotacao

import android.app.Dialog
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
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
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
import com.google.firebase.firestore.FirebaseFirestore
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat


class Pag_home : AppCompatActivity() {

    private lateinit var tvTextVersiculoAntigo: TextView
    private lateinit var tvNumeroVersiculoAntigo: TextView
    private lateinit var tvTextVersiculoNovo: TextView
    private lateinit var tvNumeroVersiculoNovo: TextView

    private val db = FirebaseFirestore.getInstance()

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
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, Pag_home::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // VERIFICA LOGIN

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            val intentLogin = Intent(this, Pag_entrar::class.java)
            intentLogin.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intentLogin)
            finish()
            return
        } else {
            // Atualiza token FCM do usuário logado
            atualizarTokenFCM(user.uid)
        }


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
        btnCruz.setOnClickListener { startActivity(Intent(this, Mensagens_historico::class.java)) }
        btnBiblia.setOnClickListener { startActivity(Intent(this, Mensagens::class.java)) }

        buscarVersiculosDoDia()
        carregarMensagemDoDia()
        carregarLemaDoAno()
        monitorarNovaMensagem(user.uid)

        // PERMISSÃO PARA NOTIFICAÇÃO (SEM ENVIO DE TESTE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
        }
    }


    private fun atualizarTokenFCM(uid: String) {
        com.google.firebase.messaging.FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                val db = FirebaseFirestore.getInstance()
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


    private fun monitorarNovaMensagem(uid: String) {
        val db = FirebaseFirestore.getInstance()
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

                    // Envia notificação sempre que houver atualização
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