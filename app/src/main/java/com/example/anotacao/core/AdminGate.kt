package com.example.anotacao.core

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

object AdminGate {
    /**
     * Chame após setContentView():
     * AdminGate.requireAdmin(this)
     */
    fun requireAdmin(activity: AppCompatActivity) {
        activity.lifecycleScope.launch {
            // Se já existe cache, usa; senão força refresh
            val cached = SessionAuth.isAdminFlow.value
            val isAdmin = when (cached) {
                null -> {
                    try { SessionAuth.refreshClaims() } catch (_: Throwable) {}
                    SessionAuth.isAdminFlow.filterNotNull().first()
                }
                else -> cached
            }

            if (!isAdmin) {
                Toast.makeText(
                    activity,
                    "Acesso restrito ao administrador.",
                    Toast.LENGTH_SHORT
                ).show()
                activity.finish()
            }
        }
    }
}
