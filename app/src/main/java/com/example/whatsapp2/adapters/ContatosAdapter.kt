package com.example.whatsapp2.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.example.whatsapp2.databinding.ItemContatosBinding
import com.example.whatsapp2.model.Usuario
import com.squareup.picasso.Picasso

// Classe que representa o adapter do RecyclerView
class ContatosAdapter(
    private val onClick: (Usuario) -> Unit

) : Adapter<ContatosAdapter.ContatosViewHolder>() {

    private var listaContatos = emptyList<Usuario>()
    fun adicionarLista(lista: List<Usuario>) {
        listaContatos = lista
        notifyDataSetChanged()
    }

    // Classe interna que representa um ViewHolder deve ser passado uma visualização
    inner class ContatosViewHolder(
        // Recebe a visualização
        private val binding: ItemContatosBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        // Método que é executado quando o ViewHolder é criado fazendo a conexão com a interface
        fun bind(usuario: Usuario) {
            // Faz a conexão com o layout e exibe o conteúdo
            binding.textContatoNome.text = usuario.nome
            Picasso
                .get()
                .load(usuario.foto)
                .into(binding.imageContatoFoto)

            binding.clItemContato.setOnClickListener {
                onClick(usuario)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContatosViewHolder {
        // Infla o layout do item de contato
        // Cria uma instancia de ContatosViewHolder passando a visualização
        //Precisa de um itemContatosBinding

        //Recuperar o inflater do parent
        val inflater = LayoutInflater.from(parent.context)
        // criar o binding
        val itemView = ItemContatosBinding.inflate(
            inflater, parent, false
        )
        return ContatosViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        // Retorna o tamanho da lista de contatos
        return listaContatos.size
    }

    override fun onBindViewHolder(holder: ContatosViewHolder, position: Int) {
        val usuario = listaContatos[position]
        // Passa o usuário para o ViewHolder na função bind do ViewHolder
        holder.bind(usuario)

    }
}