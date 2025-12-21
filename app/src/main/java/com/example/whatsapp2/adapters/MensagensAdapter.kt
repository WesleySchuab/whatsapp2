package com.example.whatsapp2.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.whatsapp2.databinding.ItensMensagensDestinarioBinding
import com.example.whatsapp2.databinding.ItensMensagensRemetenteBinding
import com.example.whatsapp2.model.Mensagem
import com.google.firebase.auth.FirebaseAuth

class MensagensAdapter(
    private val idUsuarioLogado: String,
    private val onDelete: ((Mensagem) -> Unit)? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var listaMensagens = mutableListOf<Mensagem>()

    companion object {
        private const val TIPO_REMETENTE = 0
        private const val TIPO_DESTINATARIO = 1
        private const val TAG = "MensagensAdapter"
    }

    fun adicionarLista(lista: MutableList<Mensagem>) {
        listaMensagens = lista
        Log.i(TAG, "adicionarLista() chamado com ${lista.size} mensagens. ID logado: $idUsuarioLogado")
        lista.forEachIndexed { index, msg ->
            Log.i(TAG, "[$index] idUsuario=${msg.idUsuario}, mensagem='${msg.mensagem}'")
        }
        notifyDataSetChanged()
    }

    inner class MensagensRemetenteViewHolder(
        private val binding: ItensMensagensRemetenteBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(mensagem: Mensagem) {
            binding.textMensagemRemetente.text = mensagem.mensagem
            Log.i(TAG, "MensagensRemetenteViewHolder bind: ${mensagem.mensagem}")
            
            binding.textMensagemRemetente.setOnClickListener {
                Toast.makeText(it.context, "Clicou na mensagem!", Toast.LENGTH_SHORT).show()
                onDelete?.invoke(mensagem)
            }
            
            binding.textMensagemRemetente.setOnLongClickListener {
                Toast.makeText(it.context, "Long click detectado!", Toast.LENGTH_SHORT).show()
                onDelete?.invoke(mensagem)
                true
            }
        }
    }

    inner class MensagensDestinatarioViewHolder(
        private val binding: ItensMensagensDestinarioBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(mensagem: Mensagem) {
            binding.textMensagemDestinatario.text = mensagem.mensagem
            Log.i(TAG, "MensagensDestinatarioViewHolder bind: ${mensagem.mensagem}")
            
            binding.textMensagemDestinatario.setOnClickListener {
                Toast.makeText(it.context, "Clicou na mensagem!", Toast.LENGTH_SHORT).show()
                onDelete?.invoke(mensagem)
            }
            
            binding.textMensagemDestinatario.setOnLongClickListener {
                Toast.makeText(it.context, "Long click detectado!", Toast.LENGTH_SHORT).show()
                onDelete?.invoke(mensagem)
                true
            }
        }
    }

    override fun getItemCount(): Int = listaMensagens.size

    override fun getItemViewType(position: Int): Int {
        val mensagem = listaMensagens[position]
        val tipo = if (mensagem.idUsuario == idUsuarioLogado) {
            TIPO_REMETENTE
        } else {
            TIPO_DESTINATARIO
        }
        Log.i(TAG, "getItemViewType[$position]: idUsuario=${mensagem.idUsuario}, idLogado=$idUsuarioLogado, tipo=$tipo")
        return tipo
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TIPO_REMETENTE) {
            val binding = ItensMensagensRemetenteBinding.inflate(inflater, parent, false)
            Log.i(TAG, "Criado MensagensRemetenteViewHolder")
            MensagensRemetenteViewHolder(binding)
        } else {
            val binding = ItensMensagensDestinarioBinding.inflate(inflater, parent, false)
            Log.i(TAG, "Criado MensagensDestinatarioViewHolder")
            MensagensDestinatarioViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val mensagem = listaMensagens[position]
        when (holder) {
            is MensagensRemetenteViewHolder -> holder.bind(mensagem)
            is MensagensDestinatarioViewHolder -> holder.bind(mensagem)
        }
    }
}