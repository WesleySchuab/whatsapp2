package com.example.whatsapp2.adapters

import com.example.whatsapp2.databinding.ItensMensagensDestinarioBinding
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.whatsapp2.databinding.ItensMensagensRemetenteBinding
import com.example.whatsapp2.model.Mensagem
import com.example.whatsapp2.utils.Constantes
import com.google.firebase.auth.FirebaseAuth

class MensagensAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var listaMensagens = emptyList<Mensagem>()
    fun adicionarLista(lista: List<Mensagem>) {
        listaMensagens = lista
        notifyDataSetChanged()
    }

    class MensagensRemetenteViewHolder(
        private val binding: ItensMensagensRemetenteBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(mensagem: Mensagem) {
            binding.textMensagemRemetente.text = mensagem.mensagem
        }
    }

    class MensagensDestinatarioViewHolder(
        private val binding: ItensMensagensDestinarioBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(mensagem: Mensagem) {
            binding.textMensagemDestinatario.text = mensagem.mensagem
        }
    }

    override fun getItemViewType(position: Int): Int {
        val mensagem = listaMensagens[position]
        val idUsuarioLogado = FirebaseAuth.getInstance().currentUser?.uid
        if (idUsuarioLogado == mensagem.idUsuario) {
            return Constantes.TIPO_REMTENTE
        }
        return Constantes.TIPO_DESTINATARIO
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == Constantes.TIPO_REMTENTE) {
            val inflater = LayoutInflater.from(parent.context)
            val itemView = ItensMensagensRemetenteBinding.inflate(
                inflater, parent, false
            )
            return MensagensRemetenteViewHolder(itemView)
        } else {
            val inflater = LayoutInflater.from(parent.context)
            val itemView = ItensMensagensDestinarioBinding.inflate(
                inflater, parent, false
            )
            return MensagensDestinatarioViewHolder(itemView)
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is MensagensRemetenteViewHolder -> {
                holder.bind(listaMensagens[position])
            }

            is MensagensDestinatarioViewHolder -> {
                holder.bind(listaMensagens[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return listaMensagens.size
    }
}