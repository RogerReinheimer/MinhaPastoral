package com.example.anotacao

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class Lema(val titulo: String, val data: String, val texto: String)

class LemaAdapter(private val listaLemas: List<Lema>) :
    RecyclerView.Adapter<LemaAdapter.LemaViewHolder>() {

    class LemaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtTitulo: TextView = itemView.findViewById(R.id.tv_titulo_layout_salvo)
        val txtData: TextView = itemView.findViewById(R.id.tv_data_layout_salvo)
        val txtTexto: TextView = itemView.findViewById(R.id.tv_conteudo_layout_salvo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LemaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_mensagem_salva, parent, false)
        return LemaViewHolder(view)
    }

    override fun onBindViewHolder(holder: LemaViewHolder, position: Int) {
        val lema = listaLemas[position]
        holder.txtTitulo.text = lema.titulo
        holder.txtData.text = lema.data
        holder.txtTexto.text = lema.texto
        holder.txtTexto.visibility = View.GONE

        holder.itemView.setOnClickListener {
            holder.txtTexto.visibility =
                if (holder.txtTexto.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }
    }

    override fun getItemCount(): Int = listaLemas.size
}
