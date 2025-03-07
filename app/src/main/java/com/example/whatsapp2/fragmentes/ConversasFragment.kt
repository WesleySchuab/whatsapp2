package com.example.whatsapp2.fragmentes

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.whatsapp2.activities.MensagensActivity
import com.example.whatsapp2.adapters.ConversasAdapter
import com.example.whatsapp2.databinding.FragmentConversasBinding
import com.example.whatsapp2.model.Conversa
import com.example.whatsapp2.model.Usuario
import com.example.whatsapp2.utils.Constantes
import com.example.whatsapp2.utils.exibirMensagem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query


class ConversasFragment : Fragment() {

    private lateinit var binding: FragmentConversasBinding
    private lateinit var eventoSnapshot: ListenerRegistration

    private val firebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
    private val firestore by lazy {
        FirebaseFirestore.getInstance()
    }

    private lateinit var conversasAdapter: ConversasAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentConversasBinding.inflate(inflater, container, false)
        //Instancia o adapter
        conversasAdapter = ConversasAdapter { conversa ->
            val intent = Intent(context, MensagensActivity::class.java)

            val usuario = Usuario(
                id = conversa.idUsuarioDestinatario,
                nome = conversa.nome,
                foto = conversa.foto
            )
            intent.putExtra("dadosDestinatario", usuario)
            //intent.putExtra("origem", Constantes.ORIGEM_CONVERSA)
            startActivity(intent)
        }
        binding.rvConversa.adapter = conversasAdapter
        binding.rvConversa.layoutManager = LinearLayoutManager(context)
        binding.rvConversa.addItemDecoration(
            DividerItemDecoration(
                context,
                LinearLayoutManager.VERTICAL
            )
        )
        return binding.root
    }


    override fun onStart() {
        super.onStart()
        adicionarListenerConversas()
    }

    override fun onDestroy() {
        super.onDestroy()
        eventoSnapshot.remove()

    }

    private fun adicionarListenerConversas() {
        val idUsuarioRemetente = firebaseAuth.currentUser?.uid
        if (idUsuarioRemetente != null) {
            // activity?.exibirMensagem("Usuário encontrado")
            eventoSnapshot = firestore
                .collection(Constantes.CONVERSAS)
                .document(idUsuarioRemetente)
                .collection(Constantes.ULTIMAS_CONVERSAS)
                .orderBy("data",Query.Direction.DESCENDING)
                .addSnapshotListener { querySnapshot, erro ->
                    if (erro != null) {
                        activity?.exibirMensagem("Erro ao carregar as conversas")
                    }
                    val listaConversas = mutableListOf<Conversa>()
                    val documento = querySnapshot?.documents
                    documento?.forEach { documentSnapshot ->
                        val conversa = documentSnapshot.toObject(Conversa::class.java)
                        if (conversa != null) {
                            listaConversas.add(conversa)
                            Log.i(
                                "listaConversas",
                                "Conversas ${conversa.nome} - ${conversa.ultimaMensagem} : "
                            )
                        }
                    }
                    if (listaConversas.isNotEmpty()) {
                        // Atualiza o adapter com a lista de conversas
                        conversasAdapter.adicionarLista(listaConversas)

                    }
                }
        } else {
            activity?.exibirMensagem("Usuário não encontrado")

        }
    }


}