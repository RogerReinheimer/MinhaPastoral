package com.example.anotacao.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * A anotação @Database define esta classe como o banco de dados principal.
 * entities = [User::class] informa ao Room qual(is) tabela(s) este banco terá.
 * version = 1 é a versão do nosso banco. Se no futuro mudarmos a estrutura
 * (ex: adicionar uma coluna), teremos que incrementar este número.
 */
@Database(entities = [User::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    // Esta função abstrata permite que o resto do app acesse nosso UserDao.
    abstract fun userDao(): UserDao

    /**
     * O 'companion object' é um padrão para tornar algo acessível sem precisar
     * criar uma instância da classe. Usamos para garantir que teremos apenas
     * UMA instância do banco de dados em todo o app (padrão Singleton).
     */
    companion object {
        /**
         * A anotação @Volatile garante que o valor da variável INSTANCE
         * seja sempre o mais atualizado e visível para todas as threads.
         */
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // Verifica se a instância já existe. Se sim, a retorna.
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            /**
             * Se a instância não existe, entramos em um bloco 'synchronized'.
             * Isso garante que apenas uma thread por vez possa criar o banco,
             * evitando a criação de duas instâncias ao mesmo tempo.
             */
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database" // Nome do arquivo do banco de dados
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}