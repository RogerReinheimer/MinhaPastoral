package com.example.anotacao

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    companion object {
        private const val TAG = "FCMService"
        private const val CHANNEL_ID = "canal_geral"
        private const val CHANNEL_NAME = "Canal Geral"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "Mensagem recebida de: ${remoteMessage.from}")

        remoteMessage.notification?.let {
            val title = it.title ?: "Nova mensagem"
            val body = it.body ?: ""
            Log.d(TAG, "Notificação: $title - $body")
            enviarNotificacao(title, body)
        }

        remoteMessage.data.isNotEmpty().let {
            Log.d(TAG, "Dados da mensagem: ${remoteMessage.data}")
        }
    }

    private fun enviarNotificacao(title: String, body: String) {
        criarCanalNotificacao()

        val intent = Intent(this, Pag_home::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val notificationId = System.currentTimeMillis().toInt()
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId, builder.build())

        Log.d(TAG, "Notificação enviada: ID=$notificationId")
    }

    private fun criarCanalNotificacao() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (manager.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Canal para notificações gerais do app"
                }
                manager.createNotificationChannel(channel)
                Log.d(TAG, "Canal de notificação criado")
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Novo token FCM: $token")

        val uid = auth.currentUser?.uid
        if (uid != null) {
            db.collection("users").document(uid)
                .update("fcmToken", token)
                .addOnSuccessListener {
                    Log.d(TAG, "Token FCM atualizado no Firestore")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Erro ao atualizar token FCM", e)
                }
        } else {
            Log.w(TAG, "Usuário não autenticado, token não foi salvo")
        }
    }
}
