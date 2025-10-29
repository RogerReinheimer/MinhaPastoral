
package com.example.anotacao

import android.Manifest
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

    companion object {
        private const val TAG = "Configuracoes"
        private const val MAX_IMAGE_DIMENSION = 1024 // Redimensionar imagens grandes
        private const val FOTO_PERFIL_FILENAME = "foto_perfil.jpg"
    }

    // Activity Result API - substitui startActivityForResult
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

    // Launcher para solicitar permissões
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
        // Remover foto
        btnRemoverFoto.setOnClickListener {
            removerFoto()
        }

        // Toggle de visibilidade de senha
        setupPasswordVisibilityToggle(etSenha, ivToggleSenha)
        setupPasswordVisibilityToggle(etSenhaAtual, ivToggleSenhaAtual)

        // Navegação
        btnMenu.setOnClickListener { mostrarSheetLateral() }
        btnFlutuante.setOnClickListener { mostrarSheetOpcoes() }
        btnBiblia.setOnClickListener {
            startActivity(Intent(this, Mensagens::class.java))
        }
        btnPagHome.setOnClickListener {
            startActivity(Intent(this, Pag_home::class.java))
        }
        btnCruz.setOnClickListener {
            startActivity(Intent(this, Mensagens_historico::class.java))
        }

        // Salvar alterações
        btnSalvar.setOnClickListener {
            salvarAlteracoes()
        }

        // Alterar foto - usa permissões e Activity Result API
        txtAlterarFoto.setOnClickListener {
            verificarPermissaoESelecionarImagem()
        }

        // Notificações
        configurarNotificacoes()
    }

    /**
     * Verifica se a permissão de leitura de imagens está concedida
     * Android 13+ (API 33+): READ_MEDIA_IMAGES
     * Android 6-12 (API 23-32): READ_EXTERNAL_STORAGE
     */
    private fun verificarPermissaoESelecionarImagem() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED -> {
                // Permissão já concedida
                abrirSeletorImagem()
            }
            shouldShowRequestPermissionRationale(permission) -> {
                // Explicar por que a permissão é necessária
                Toast.makeText(
                    this,
                    "Permissão necessária para acessar suas fotos.",
                    Toast.LENGTH_LONG
                ).show()
                requestPermissionLauncher.launch(permission)
            }
            else -> {
                // Solicitar permissão
                requestPermissionLauncher.launch(permission)
            }
        }
    }

    /**
     * Abre o seletor de imagens usando Activity Result API
     */
    private fun abrirSeletorImagem() {
        try {
            pickImageLauncher.launch("image/*")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao abrir seletor de imagens", e)
            Toast.makeText(this, "Erro ao abrir galeria.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Processa a imagem selecionada: valida, redimensiona e salva
     * Compatível com Google Drive, Google Fotos e galeria local
     */
    private fun processarImagemSelecionada(uri: Uri) {
        Log.d(TAG, "Processando imagem: $uri")

        // Mostrar loading para URIs remotas (Drive/Fotos)
        val isRemoteUri = uri.scheme == "content" &&
                (uri.authority?.contains("com.google") == true ||
                        uri.authority?.contains("drive") == true)

        if (isRemoteUri) {
            Toast.makeText(this, "Baixando imagem...", Toast.LENGTH_SHORT).show()
        }

        try {
            // 1. Abrir InputStream da URI (funciona com Drive/Fotos)
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

            // 2. Copiar bytes para buffer (necessário para URIs remotas)
            val bytes = inputStream.readBytes()
            inputStream.close()

            if (bytes.isEmpty()) {
                Log.e(TAG, "Dados vazios da URI: $uri")
                Toast.makeText(
                    this,
                    "Imagem vazia ou corrompida.",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }

            Log.d(TAG, "Imagem carregada: ${bytes.size} bytes")

            // 3. Decodificar bytes para Bitmap
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

            // 4. Redimensionar se necessário (evitar OOM)
            val bitmapRedimensionado = redimensionarBitmap(bitmap)

            // 5. Salvar localmente
            val sucesso = salvarImagemLocal(bitmapRedimensionado)

            if (sucesso) {
                // 5. Limpar cache do Glide antes de carregar nova foto
                Glide.get(this).clearMemory()

                // 6. Carregar imagem salva no ImageView
                carregarFotoLocal()
                btnRemoverFoto.visibility = View.VISIBLE
                Toast.makeText(this, "Foto salva com sucesso!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Erro ao salvar a foto.", Toast.LENGTH_SHORT).show()
            }

            // Liberar memória
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

    /**
     * Redimensiona bitmap se exceder MAX_IMAGE_DIMENSION
     * Mantém proporção de aspecto
     */
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

    /**
     * Salva bitmap no diretório interno do app (filesDir)
     * Retorna true se salvou com sucesso e arquivo não está vazio
     */
    private fun salvarImagemLocal(bitmap: Bitmap): Boolean {
        return try {
            val file = File(filesDir, FOTO_PERFIL_FILENAME)

            // Salvar bitmap como JPEG
            FileOutputStream(file).use { out ->
                val comprimido = bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                if (!comprimido) {
                    Log.e(TAG, "Falha ao comprimir bitmap")
                    return false
                }
            }

            // Validar que o arquivo foi salvo corretamente
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

            // Persistir caminho em SharedPreferences
            val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
            prefs.edit().putString("foto_local", file.absolutePath).apply()

            true

        } catch (e: Exception) {
            Log.e(TAG, "Erro ao salvar imagem local", e)
            false
        }
    }

    /**
     * Carrega foto do armazenamento local usando Glide
     * IMPORTANTE: Desabilita cache para permitir trocas de foto
     */
    private fun carregarFotoLocal() {
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val caminhoFoto = prefs.getString("foto_local", null)

        if (caminhoFoto != null) {
            val file = File(caminhoFoto)
            if (file.exists() && file.length() > 0) {
                Log.d(TAG, "Carregando foto local: $caminhoFoto")
                Glide.with(this)
                    .load(file)
                    .skipMemoryCache(true) // Não usar cache de memória
                    .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE) // Não usar cache de disco
                    .circleCrop()
                    .into(ivUsuario)
                btnRemoverFoto.visibility = View.VISIBLE
                return
            } else {
                Log.w(TAG, "Arquivo de foto não existe ou está vazio: $caminhoFoto")
                // Limpar referência inválida
                prefs.edit().remove("foto_local").apply()
            }
        }

        // Foto padrão
        ivUsuario.setImageResource(R.drawable.usuario_grande)
        btnRemoverFoto.visibility = View.GONE
    }

    /**
     * Remove foto do armazenamento local
     * IMPORTANTE: Limpa cache do Glide para garantir que nova foto será carregada
     */
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

        // Limpar SharedPreferences
        prefs.edit().remove("foto_local").apply()

        // Limpar cache do Glide
        Glide.get(this).clearMemory()
        Thread {
            Glide.get(this).clearDiskCache()
        }.start()

        // Resetar para imagem padrão
        ivUsuario.setImageResource(R.drawable.usuario_grande)
        btnRemoverFoto.visibility = View.GONE
        Toast.makeText(this, "Foto removida.", Toast.LENGTH_SHORT).show()
    }

    private fun carregarDadosUsuario() {
        val usuario = FirebaseAuth.getInstance().currentUser
        val uid = usuario?.uid

        if (uid != null) {
            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        etUsuario.setText(document.getString("username") ?: "")
                        // Não carrega foto do Firebase - usa apenas local
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Erro ao carregar dados do usuário", e)
                    Toast.makeText(this, "Erro ao carregar dados do usuário", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun salvarAlteracoes() {
        val usuario = FirebaseAuth.getInstance().currentUser ?: return
        val novoNome = etUsuario.text.toString().trim()
        val novaSenha = etSenha.text.toString()
        val senhaAtual = etSenhaAtual.text.toString()

        // Atualizar nome
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

        // Atualizar senha
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

            val uid = FirebaseAuth.getInstance().currentUser?.uid

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

        // Carregar nome do usuário
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    txtPerfilNome.text = "Olá, ${document.getString("username") ?: "usuário"}!"
                }
        }

        // Carregar foto local (mesma lógica da tela de configurações)
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val caminhoFoto = prefs.getString("foto_local", null)

        if (caminhoFoto != null) {
            val file = File(caminhoFoto)
            if (file.exists() && file.length() > 0) {
                // Foto existe, carregar com Glide (SEM CACHE)
                Glide.with(this)
                    .load(file)
                    .skipMemoryCache(true) // Desabilita cache de memória
                    .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE) // Desabilita cache de disco
                    .circleCrop()
                    .placeholder(R.drawable.img_3) // Imagem temporária enquanto carrega
                    .error(R.drawable.img_3)       // Imagem caso falhe
                    .into(imgPerfil)
                Log.d(TAG, "Foto carregada no menu: $caminhoFoto")
            } else {
                // Arquivo não existe ou está vazio
                Log.w(TAG, "Foto não encontrada no menu, usando padrão")
                imgPerfil.setImageResource(R.drawable.img_3)
            }
        } else {
            // Sem foto salva, usar padrão
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