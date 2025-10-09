package com.example.anotacao.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * A anotação @Dao marca esta interface como um Data Access Object.
 * É aqui que definimos todas as nossas interações com o banco de dados.
 */
@Dao
interface UserDao {

    /**
     * A anotação @Insert define uma função para inserir dados.
     * onConflict = OnConflictStrategy.IGNORE significa que se tentarmos inserir um
     * usuário que já existe (com a mesma chave primária), a operação será ignorada.
     * A palavra 'suspend' indica que esta função é assíncrona e deve ser chamada
     * a partir de uma Coroutine, para não travar a interface do usuário.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(user: User)

    /**
     * A anotação @Query nos permite escrever comandos SQL para ler dados.
     * Esta função busca na tabela 'users' um usuário cujo nome de usuário
     * corresponda ao parâmetro :username.
     * Retorna um User? (nulável), pois o usuário pode não ser encontrado.
     */
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun findByUsername(username: String): User?

    /**
     * Função extra que será útil para verificar se um e-mail já foi
     * cadastrado por outro usuário.
     */
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): User?
}