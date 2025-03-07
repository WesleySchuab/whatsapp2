package com.example.whatsapp2.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.example.whatsapp2.databinding.ItemConversasBinding
import com.example.whatsapp2.model.Conversa
import com.squareup.picasso.Picasso

class ConversasAdapter(
    private val onClick: (Conversa) -> Unit

) : Adapter<ConversasAdapter.ConversasViewHolder>()  {

    private var listaConversas = emptyList<Conversa>()
    fun adicionarLista(lista: List<Conversa>) {
        listaConversas = lista
        notifyDataSetChanged()
    }
    // Classe interna que representa um ViewHolder deve ser passado uma visualização
    inner class ConversasViewHolder(
        // Recebe a visualização
        private val binding: ItemConversasBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        // Método que é executado quando o ViewHolder é criado fazendo a conexão com a interface
        fun bind(conversa: Conversa) {
            // Faz a conexão com o layout e exibe o conteúdo
            binding.textConversaNome.text = conversa.nome
            binding.textConversaMensagem.text = conversa.ultimaMensagem
            Picasso
                .get()
                .load(conversa.foto)
                .into(binding.imageConversaFoto)

            binding.clItemConversa.setOnClickListener {
                onClick(conversa)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversasViewHolder {
        // Infla o layout do item de contato
        // Cria uma instancia de ContatosViewHolder passando a visualização
        //Precisa de um itemContatosBinding

        //Recuperar o inflater do parent
        val inflater = LayoutInflater.from(parent.context)
        // criar o binding
        val itemView = ItemConversasBinding.inflate(
            inflater, parent, false
        )
        return ConversasViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ConversasViewHolder, position: Int) {
        val conversa = listaConversas[position]
        // Passa o usuário para o ViewHolder na função bind do ViewHolder
        holder.bind(conversa)

    }

    override fun getItemCount(): Int {
        return listaConversas.size
    }
}