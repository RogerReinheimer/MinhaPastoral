package com.example.anotacao.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A anotação @Entity diz ao Room que esta classe representa uma tabela no banco de dados.
 * O tableName define o nome da tabela.
 */
@Entity(tableName = "users")
data class User(
    /**
     * @PrimaryKey define que o campo 'id' é a chave primária (identificador único) da tabela.
     * autoGenerate = true faz com que o ID seja gerado automaticamente para cada novo usuário.
     */
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // Nossas colunas para e-mail, nome de usuário e senha
    val email: String,
    val username: String,
    val passwordHash: String // É uma boa prática não salvar a senha em texto puro, por isso o nome
)