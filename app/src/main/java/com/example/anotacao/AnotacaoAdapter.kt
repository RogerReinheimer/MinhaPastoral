package com.example.anotacao

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class Anotacao(
    val titulo: String,
    val texto: String
)

class AnotacaoAdapter(
    private val listaAnotacoes: List<Anotacao>,
    private val isAdmin: Boolean
) : RecyclerView.Adapter<AnotacaoAdapter.AnotacaoViewHolder>() {

    class AnotacaoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtTitulo: TextView = itemView.findViewById(R.id.tv_titulo_layout_salvo)
        val txtTexto: TextView = itemView.findViewById(R.id.tv_conteudo_layout_salvo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnotacaoViewHolder {
        // Admin vê card com ações; comum vê card simples
        val layoutId = if (isAdmin) R.layout.card_mensagem_excluir else R.layout.card_mensagem_salva
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return AnotacaoViewHolder(view)
    }

    override fun onBindViewHolder(holder: AnotacaoViewHolder, position: Int) {
        val anotacao = listaAnotacoes[position]
        holder.txtTitulo.text = anotacao.titulo
        holder.txtTexto.text = anotacao.texto

        // começa oculto e expande no clique
        holder.txtTexto.visibility = View.GONE
        holder.itemView.setOnClickListener {
            holder.txtTexto.visibility =
                if (holder.txtTexto.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }
    }

    override fun getItemCount(): Int = listaAnotacoes.size
}
