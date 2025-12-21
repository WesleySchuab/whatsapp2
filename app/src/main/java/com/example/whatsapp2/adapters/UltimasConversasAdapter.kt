package com.example.whatsapp2.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.whatsapp2.databinding.ItemConversasBinding
import com.example.whatsapp2.model.Conversa
import com.squareup.picasso.Picasso

class UltimasConversasAdapter(
    private val onClick: (Conversa) -> Unit,
    private val onDelete: (Conversa) -> Unit
) : RecyclerView.Adapter<UltimasConversasAdapter.UltimaConversaViewHolder>() {

    private var listaConversas = emptyList<Conversa>()

    fun atualizarLista(lista: List<Conversa>) {
        listaConversas = lista
        notifyDataSetChanged()
    }

    inner class UltimaConversaViewHolder(
        private val binding: ItemConversasBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(conversa: Conversa) {
            binding.textConversaNome.text = conversa.nome
            binding.textConversaUltimaMensagem.text = conversa.ultimaMensagem

            if (conversa.foto.isNotEmpty()) {
                Picasso.get()
                    .load(conversa.foto)
                    .into(binding.imageConversaFoto)
            }

            binding.clItemConversa.setOnClickListener {
                onClick(conversa)
            }

            binding.btnExcluirConversa.setOnClickListener {
                onDelete(conversa)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UltimaConversaViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = ItemConversasBinding.inflate(inflater, parent, false)
        return UltimaConversaViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: UltimaConversaViewHolder, position: Int) {
        holder.bind(listaConversas[position])
    }

    override fun getItemCount(): Int = listaConversas.size
}
