package com.example.anotacao

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class Mensagem(val titulo: String, val data: String, val texto: String)

class MensagemAdapter(
    private val listaMensagens: List<Mensagem>,
    private val isAdmin: Boolean
) : RecyclerView.Adapter<MensagemAdapter.MensagemViewHolder>() {

    class MensagemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtTitulo: TextView = itemView.findViewById(R.id.tv_titulo_layout_salvo)
        val txtData: TextView = itemView.findViewById(R.id.tv_data_layout_salvo)
        val txtTexto: TextView = itemView.findViewById(R.id.tv_conteudo_layout_salvo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MensagemViewHolder {
        // ADM vê o card com ações (excluir/editar); usuário comum vê o card simples
        val layoutId = if (isAdmin) R.layout.card_mensagem_excluir else R.layout.card_mensagem_salva
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return MensagemViewHolder(view)
    }

    override fun onBindViewHolder(holder: MensagemViewHolder, position: Int) {
        val mensagem = listaMensagens[position]
        holder.txtTitulo.text = mensagem.titulo
        holder.txtData.text = mensagem.data
        holder.txtTexto.text = mensagem.texto
    }

    override fun getItemCount(): Int = listaMensagens.size
}
