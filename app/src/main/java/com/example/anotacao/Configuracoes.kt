package com.example.anotacao

import android.Manifest
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max

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
    private lateinit var btnRemoverFoto: ImageView

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    companion object {
        private const val TAG = "Configuracoes"
        private const val MAX_IMAGE_DIMENSION = 1024
        private const val FOTO_PERFIL_FILENAME = "foto_perfil.jpg"
    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            Log.d(TAG, "Imagem selecionada: $uri")
            processarImagemSelecionada(uri)
        } else {
            Log.w(TAG, "Nenhuma imagem selecionada")
            Toast.makeText(this, "Nenhuma imagem selecionada.", Toast.LENGTH_SHORT).show()
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d(TAG, "Permissão concedida")
            abrirSeletorImagem()
        } else {
            Log.w(TAG, "Permissão negada")
            Toast.makeText(
                this,
                "Permissão necessária para selecionar imagens.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configuracoes)

        inicializarViews()
        configurarListeners()
        carregarDadosUsuario()
        carregarFotoLocal()
    }

    private fun inicializarViews() {
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
        btnRemoverFoto = findViewById(R.id.btnRemoverFoto)
    }

    private fun configurarListeners() {
        // ---------- BOTÃO REMOVER FOTO (200ms, 0.9) ----------
        btnRemoverFoto.setOnClickListener {
            val scaleX = ObjectAnimator.ofFloat(btnRemoverFoto, "scaleX", 1f, 0.9f, 1f)
            val scaleY = ObjectAnimator.ofFloat(btnRemoverFoto, "scaleY", 1f, 0.9f, 1f)
            AnimatorSet().apply {
                playTogether(scaleX, scaleY)
                duration = 200
                start()
            }
            removerFoto()
        }

        // ---------- TOGGLE SENHA (200ms, 0.9) ----------
        setupPasswordVisibilityToggle(etSenha, ivToggleSenha)
        setupPasswordVisibilityToggle(etSenhaAtual, ivToggleSenhaAtual)

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

        btnFlutuante.setOnClickListener {
            val scaleX = ObjectAnimator.ofFloat(btnFlutuante, "scaleX", 1f, 0.8f, 1f)
            val scaleY = ObjectAnimator.ofFloat(btnFlutuante, "scaleY", 1f, 0.8f, 1f)
            AnimatorSet().apply {
                playTogether(scaleX, scaleY)
                duration = 300
                start()
            }
            mostrarSheetOpcoes()
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

        btnPagHome.setOnClickListener {
            val scaleX = ObjectAnimator.ofFloat(btnPagHome, "scaleX", 1f, 0.8f, 1f)
            val scaleY = ObjectAnimator.ofFloat(btnPagHome, "scaleY", 1f, 0.8f, 1f)
            AnimatorSet().apply {
                playTogether(scaleX, scaleY)
                duration = 300
                start()
            }
            startActivity(Intent(this, Pag_home::class.java))
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

        // ---------- BOTÃO SALVAR (200ms, 0.9) ----------
        btnSalvar.setOnClickListener {
            val scaleX = ObjectAnimator.ofFloat(btnSalvar, "scaleX", 1f, 0.9f, 1f)
            val scaleY = ObjectAnimator.ofFloat(btnSalvar, "scaleY", 1f, 0.9f, 1f)
            AnimatorSet().apply {
                playTogether(scaleX, scaleY)
                duration = 200
                start()
            }
            salvarAlteracoes()
        }

        // ---------- ALTERAR FOTO (200ms, 0.9) ----------
        txtAlterarFoto.setOnClickListener {
            val scaleX = ObjectAnimator.ofFloat(txtAlterarFoto, "scaleX", 1f, 0.9f, 1f)
            val scaleY = ObjectAnimator.ofFloat(txtAlterarFoto, "scaleY", 1f, 0.9f, 1f)
            AnimatorSet().apply {
                playTogether(scaleX, scaleY)
                duration = 200
                start()
            }
            verificarPermissaoESelecionarImagem()
        }

        configurarNotificacoes()
    }

    private fun verificarPermissaoESelecionarImagem() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED -> {
                abrirSeletorImagem()
            }
            shouldShowRequestPermissionRationale(permission) -> {
                Toast.makeText(
                    this,
                    "Permissão necessária para acessar suas fotos.",
                    Toast.LENGTH_LONG
                ).show()
                requestPermissionLauncher.launch(permission)
            }
            else -> {
                requestPermissionLauncher.launch(permission)
            }
        }
    }

    private fun abrirSeletorImagem() {
        try {
            pickImageLauncher.launch("image/*")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao abrir seletor de imagens", e)
            Toast.makeText(this, "Erro ao abrir galeria.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun processarImagemSelecionada(uri: Uri) {
        Log.d(TAG, "Processando imagem: $uri")

        val isRemoteUri = uri.scheme == "content" &&
                (uri.authority?.contains("com.google") == true ||
                        uri.authority?.contains("drive") == true)

        if (isRemoteUri) {
            Toast.makeText(this, "Baixando imagem...", Toast.LENGTH_SHORT).show()
        }

        try {
            val inputStream = contentResolver.openInputStream(uri)
            if (inputStream == null) {
                Log.e(TAG, "InputStream é nulo para URI: $uri")
                Toast.makeText(
                    this,
                    "Não foi possível acessar a imagem. Tente novamente.",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }

            val bytes = inputStream.readBytes()
            inputStream.close()

            if (bytes.isEmpty()) {
                Log.e(TAG, "Dados vazios da URI: $uri")
                Toast.makeText(this, "Imagem vazia ou corrompida.", Toast.LENGTH_SHORT).show()
                return
            }

            Log.d(TAG, "Imagem carregada: ${bytes.size} bytes")

            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

            if (bitmap == null) {
                Log.e(TAG, "Falha ao decodificar bitmap da URI: $uri")
                Toast.makeText(
                    this,
                    "Falha ao processar a imagem. Tente outra foto.",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }

            Log.d(TAG, "Bitmap decodificado: ${bitmap.width}x${bitmap.height}")

            val bitmapRedimensionado = redimensionarBitmap(bitmap)
            val sucesso = salvarImagemLocal(bitmapRedimensionado)

            if (sucesso) {
                Glide.get(this).clearMemory()
                carregarFotoLocal()
                btnRemoverFoto.visibility = View.VISIBLE
                Toast.makeText(this, "Foto salva com sucesso!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Erro ao salvar a foto.", Toast.LENGTH_SHORT).show()
            }

            if (bitmap != bitmapRedimensionado) {
                bitmap.recycle()
            }

        } catch (e: OutOfMemoryError) {
            Log.e(TAG, "Memória insuficiente ao processar imagem", e)
            Toast.makeText(
                this,
                "Imagem muito grande. Tente uma foto menor.",
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao processar imagem", e)
            Toast.makeText(this, "Erro ao processar foto. Tente novamente.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun redimensionarBitmap(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= MAX_IMAGE_DIMENSION && height <= MAX_IMAGE_DIMENSION) {
            Log.d(TAG, "Bitmap não precisa ser redimensionado")
            return bitmap
        }

        val scale = MAX_IMAGE_DIMENSION.toFloat() / max(width, height)
        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()

        Log.d(TAG, "Redimensionando de ${width}x${height} para ${newWidth}x${newHeight}")

        val matrix = Matrix()
        matrix.postScale(scale, scale)

        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true)
    }

    private fun salvarImagemLocal(bitmap: Bitmap): Boolean {
        return try {
            val file = File(filesDir, FOTO_PERFIL_FILENAME)

            FileOutputStream(file).use { out ->
                val comprimido = bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                if (!comprimido) {
                    Log.e(TAG, "Falha ao comprimir bitmap")
                    return false
                }
            }

            if (!file.exists()) {
                Log.e(TAG, "Arquivo não existe após salvar: ${file.absolutePath}")
                return false
            }

            val tamanhoArquivo = file.length()
            if (tamanhoArquivo == 0L) {
                Log.e(TAG, "Arquivo salvo está vazio")
                file.delete()
                return false
            }

            Log.d(TAG, "Foto salva com sucesso: ${file.absolutePath} (${tamanhoArquivo} bytes)")

            val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
            prefs.edit().putString("foto_local", file.absolutePath).apply()

            true

        } catch (e: Exception) {
            Log.e(TAG, "Erro ao salvar imagem local", e)
            false
        }
    }

    private fun carregarFotoLocal() {
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val caminhoFoto = prefs.getString("foto_local", null)

        if (caminhoFoto != null) {
            val file = File(caminhoFoto)
            if (file.exists() && file.length() > 0) {
                Log.d(TAG, "Carregando foto local: $caminhoFoto")
                Glide.with(this)
                    .load(file)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
                    .circleCrop()
                    .into(ivUsuario)
                btnRemoverFoto.visibility = View.VISIBLE
                return
            } else {
                Log.w(TAG, "Arquivo de foto não existe ou está vazio: $caminhoFoto")
                prefs.edit().remove("foto_local").apply()
            }
        }

        ivUsuario.setImageResource(R.drawable.usuario_grande)
        btnRemoverFoto.visibility = View.GONE
    }

    private fun removerFoto() {
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val caminhoFoto = prefs.getString("foto_local", null)

        if (caminhoFoto != null) {
            val file = File(caminhoFoto)
            if (file.exists()) {
                val deletado = file.delete()
                Log.d(TAG, "Foto deletada: $deletado")
            }
        }

        prefs.edit().remove("foto_local").apply()

        Glide.get(this).clearMemory()
        Thread {
            Glide.get(this).clearDiskCache()
        }.start()

        ivUsuario.setImageResource(R.drawable.usuario_grande)
        btnRemoverFoto.visibility = View.GONE
        Toast.makeText(this, "Foto removida.", Toast.LENGTH_SHORT).show()
    }

    private fun carregarDadosUsuario() {
        val usuario = auth.currentUser
        val uid = usuario?.uid

        if (uid != null) {
            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        etUsuario.setText(document.getString("username") ?: "")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Erro ao carregar dados do usuário", e)
                    Toast.makeText(this, "Erro ao carregar dados do usuário", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun salvarAlteracoes() {
        val usuario = auth.currentUser ?: return
        val novoNome = etUsuario.text.toString().trim()
        val novaSenha = etSenha.text.toString()
        val senhaAtual = etSenhaAtual.text.toString()

        if (novoNome.isNotEmpty()) {
            db.collection("users").document(usuario.uid)
                .update("username", novoNome)
                .addOnSuccessListener {
                    Toast.makeText(this, "Nome atualizado!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Erro ao atualizar nome", e)
                    Toast.makeText(this, "Erro ao atualizar nome.", Toast.LENGTH_SHORT).show()
                }
        }

        val email = usuario.email
        if (novaSenha.isNotEmpty() && senhaAtual.isNotEmpty() && !email.isNullOrEmpty()) {
            val credential = EmailAuthProvider.getCredential(email, senhaAtual)
            usuario.reauthenticate(credential).addOnCompleteListener { authTask ->
                if (authTask.isSuccessful) {
                    usuario.updatePassword(novaSenha).addOnCompleteListener { updateTask ->
                        if (updateTask.isSuccessful) {
                            Toast.makeText(this, "Senha atualizada!", Toast.LENGTH_SHORT).show()
                            etSenha.text.clear()
                            etSenhaAtual.text.clear()
                        } else {
                            Log.e(TAG, "Erro ao atualizar senha", updateTask.exception)
                            Toast.makeText(this, "Erro ao atualizar senha.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Senha atual incorreta.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun configurarNotificacoes() {
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        cbNotificacoes.isChecked = prefs.getBoolean("receber_notificacoes", false)

        cbNotificacoes.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("receber_notificacoes", isChecked).apply()

            val uid = auth.currentUser?.uid

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
                Log.d(TAG, "Foto carregada no menu: $caminhoFoto")
            } else {
                Log.w(TAG, "Foto não encontrada no menu, usando padrão")
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

    private fun setupPasswordVisibilityToggle(editText: EditText, toggleIcon: ImageView) {
        toggleIcon.setOnClickListener {
            val scaleX = ObjectAnimator.ofFloat(toggleIcon, "scaleX", 1f, 0.9f, 1f)
            val scaleY = ObjectAnimator.ofFloat(toggleIcon, "scaleY", 1f, 0.9f, 1f)
            AnimatorSet().apply {
                playTogether(scaleX, scaleY)
                duration = 200
                start()
            }

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
