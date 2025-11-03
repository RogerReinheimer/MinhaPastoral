package com.example.anotacao.core

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.tasks.await

/**
 * Gerencia o estado de autenticação e flag de administrador.
 * - Armazena o admin em cache via StateFlow.
 * - Atualiza quando o usuário loga ou quando forçado via refreshClaims().
 */
object SessionAuth {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    /** Flow observável: null = desconhecido; true/false = definido */
    val isAdminFlow = MutableStateFlow<Boolean?>(null)

    /**
     * Recarrega a flag de admin a partir do Firestore ou custom claims.
     * Deve ser chamado no login e no início das Activities principais.
     */
    suspend fun refreshClaims() {
        val user = auth.currentUser
        if (user == null) {
            Log.w("SessionAuth", "Nenhum usuário logado — resetando flag admin.")
            isAdminFlow.value = false
            return
        }

        try {
            // 🔹 Primeiro tenta pelas custom claims (mais seguras)
            user.getIdToken(true).await()
            val claims = user.getIdToken(false).await().claims
            val adminClaim = claims["admin"] as? Boolean
            if (adminClaim != null) {
                isAdminFlow.value = adminClaim
                Log.d("SessionAuth", "Admin flag via custom claim: $adminClaim")
                return
            }

            // 🔹 Fallback: busca no Firestore (/users/{uid})
            val snap = db.collection("users").document(user.uid).get().await()
            val adminField = snap.getBoolean("admin") ?: false
            isAdminFlow.value = adminField
            Log.d("SessionAuth", "Admin flag via Firestore: $adminField")

        } catch (e: Exception) {
            Log.e("SessionAuth", "Erro ao atualizar claims", e)
            isAdminFlow.value = false
        }
    }

    /**
     * Retorna o usuário atual (pode ser null).
     */
    fun currentUser(): FirebaseUser? = auth.currentUser

    /**
     * Força logout e reseta o estado do admin.
     */
    fun signOut() {
        try {
            auth.signOut()
        } catch (e: Exception) {
            Log.e("SessionAuth", "Erro ao fazer logout", e)
        } finally {
            isAdminFlow.value = false
        }
    }

    /**
     * Apenas utilitário para logar o estado atual no Logcat.
     */
    fun debugLogState(tag: String = "SessionAuth") {
        Log.d(tag, "isAdminFlow = ${isAdminFlow.value}, user = ${auth.currentUser?.email}")
    }
}
